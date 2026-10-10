package com.inventory.service;

import com.inventory.dto.response.DashboardResponse;
import com.inventory.entity.Product;
import com.inventory.entity.PurchaseOrder;
import com.inventory.entity.Sale;
import com.inventory.entity.SaleItem;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final DemandPredictionRepository predictionRepository;

    public DashboardResponse getDashboard(Long businessId) {
        List<Product> products = productRepository.findByBusinessId(businessId);

        long totalProducts = products.size();
        long totalStockUnits = products.stream().mapToLong(Product::getCurrentStock).sum();
        double inventoryValue = products.stream()
                .mapToDouble(p -> p.getCurrentStock() * p.getPurchasePrice()).sum();
        double potentialSalesValue = products.stream()
                .mapToDouble(p -> p.getCurrentStock() * p.getSellingPrice()).sum();

        long lowStock = productRepository.findLowStock(businessId).size();
        long outOfStock = productRepository.findOutOfStock(businessId).size();
        long overstock = productRepository.findOverstock(businessId).size();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        double todaysSales = saleRepository.findByBusinessIdAndSaleDateBetween(businessId, startOfToday, endOfToday)
                .stream().mapToDouble(Sale::getTotalAmount).sum();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Sale> monthSales = saleRepository.findByBusinessIdAndSaleDateBetween(businessId, startOfMonth, endOfToday);
        double monthlyRevenue = monthSales.stream().mapToDouble(Sale::getTotalAmount).sum();

        List<SaleItem> monthItems = saleItemRepository.findByBusinessAndDateRange(businessId, startOfMonth, endOfToday);
        double monthlyCost = monthItems.stream()
                .mapToDouble(si -> si.getQuantity() * (si.getProduct().getPurchasePrice())).sum();
        double monthlyProfit = monthlyRevenue - monthlyCost;

        long pendingPurchases = purchaseOrderRepository.findByBusinessIdOrderByCreatedAtDesc(businessId).stream()
                .filter(po -> po.getStatus() == PurchaseOrder.PurchaseStatus.ORDERED
                        || po.getStatus() == PurchaseOrder.PurchaseStatus.PARTIALLY_RECEIVED)
                .count();

        // Top products by revenue this month
        Map<Long, double[]> revenueByProduct = new HashMap<>(); // [revenue, unitsSold]
        Map<Long, String> nameByProduct = new HashMap<>();
        for (SaleItem si : monthItems) {
            Long pid = si.getProduct().getId();
            double[] agg = revenueByProduct.getOrDefault(pid, new double[]{0, 0});
            agg[0] += si.getQuantity() * si.getPrice();
            agg[1] += si.getQuantity();
            revenueByProduct.put(pid, agg);
            nameByProduct.put(pid, si.getProduct().getName());
        }
        List<DashboardResponse.TopProduct> topProducts = revenueByProduct.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .limit(5)
                .map(e -> new DashboardResponse.TopProduct(e.getKey(), nameByProduct.get(e.getKey()),
                        Math.round(e.getValue()[0] * 100.0) / 100.0, (int) e.getValue()[1]))
                .collect(Collectors.toList());

        List<DashboardResponse.Insight> insights = buildInsights(businessId, products);

        return new DashboardResponse(
                totalProducts, totalStockUnits, round(inventoryValue), round(potentialSalesValue),
                round(todaysSales), round(monthlyRevenue), round(monthlyProfit), pendingPurchases,
                lowStock, outOfStock, overstock, topProducts, insights
        );
    }

    private List<DashboardResponse.Insight> buildInsights(Long businessId, List<Product> products) {
        List<DashboardResponse.Insight> insights = new ArrayList<>();

        var predictions = predictionRepository.findByBusinessIdOrderByGeneratedAtDesc(businessId);
        Map<Long, com.inventory.entity.DemandPrediction> latestPerProduct = new HashMap<>();
        for (var p : predictions) {
            latestPerProduct.putIfAbsent(p.getProduct().getId(), p);
        }

        for (var entry : latestPerProduct.entrySet()) {
            var pred = entry.getValue();
            if (pred.getRiskLevel() == com.inventory.entity.DemandPrediction.RiskLevel.CRITICAL
                    || pred.getRiskLevel() == com.inventory.entity.DemandPrediction.RiskLevel.HIGH) {
                insights.add(new DashboardResponse.Insight("STOCKOUT_RISK",
                        pred.getProduct().getName() + " may run out in " +
                                Math.max(0, Math.round(pred.getDaysUntilStockout())) + " days"));
            }
            if (pred.getTrend() == com.inventory.entity.DemandPrediction.Trend.RISING) {
                insights.add(new DashboardResponse.Insight("DEMAND_SPIKE",
                        pred.getProduct().getName() + " demand is trending up"));
            }
            if (pred.getTrend() == com.inventory.entity.DemandPrediction.Trend.DECLINING) {
                insights.add(new DashboardResponse.Insight("DEMAND_DROP",
                        pred.getProduct().getName() + " sales have been declining"));
            }
        }

        for (Product p : products) {
            if (p.getCurrentStock() > p.getMaxStock()) {
                double excessValue = (p.getCurrentStock() - p.getMaxStock()) * p.getPurchasePrice();
                insights.add(new DashboardResponse.Insight("OVERSTOCK",
                        p.getName() + " has excess inventory worth " + Math.round(excessValue)));
            }
            if (p.getCurrentStock() == 0) {
                insights.add(new DashboardResponse.Insight("OUT_OF_STOCK", p.getName() + " is out of stock"));
            }
        }

        return insights.stream().limit(10).collect(Collectors.toList());
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
