package com.inventory.service;

import com.inventory.dto.response.DemandPredictionResponse;
import com.inventory.entity.*;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.DemandPredictionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Demand forecasting engine.
 *
 * Baseline model: Weighted Moving Average over the last 4 weekly buckets
 * (most recent week weighted highest). This is a statistically sound,
 * dependency-free baseline that works with no external ML service.
 * Swap out computeForecast() to call a Python ML microservice later
 * without touching any controller/repository code.
 */
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;
    private final DemandPredictionRepository predictionRepository;

    private static final int HISTORY_DAYS = 28; // 4 weeks
    private static final double[] WEEK_WEIGHTS = {0.40, 0.30, 0.20, 0.10}; // most recent first

    public DemandPredictionResponse generateForProduct(Long businessId, Long productId, int forecastPeriodDays) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.getBusiness().getId().equals(businessId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        DemandPrediction prediction = compute(product, forecastPeriodDays);
        prediction = predictionRepository.save(prediction);
        return toResponse(prediction);
    }

    public List<DemandPredictionResponse> generateForBusiness(Long businessId, int forecastPeriodDays) {
        List<Product> products = productRepository.findByBusinessId(businessId);
        return products.stream()
                .map(p -> {
                    DemandPrediction dp = compute(p, forecastPeriodDays);
                    return predictionRepository.save(dp);
                })
                .map(this::toResponse)
                .toList();
    }

    public List<DemandPredictionResponse> latestForBusiness(Long businessId) {
        return predictionRepository.findByBusinessIdOrderByGeneratedAtDesc(businessId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<DemandPredictionResponse> latestForProduct(Long productId) {
        return predictionRepository.findFirstByProductIdOrderByGeneratedAtDesc(productId).map(this::toResponse);
    }

    private DemandPrediction compute(Product product, int forecastPeriodDays) {
        LocalDateTime now = LocalDateTime.now();
        double[] weeklyTotals = new double[4];

        for (int week = 0; week < 4; week++) {
            LocalDateTime end = now.minusDays((long) week * 7);
            LocalDateTime start = end.minusDays(7);
            List<SaleItem> items = saleItemRepository.findByProductAndDateRange(product.getId(), start, end);
            weeklyTotals[week] = items.stream().mapToInt(SaleItem::getQuantity).sum();
        }

        double totalHistoricalUnits = weeklyTotals[0] + weeklyTotals[1] + weeklyTotals[2] + weeklyTotals[3];

        double weightedWeeklyDemand = 0.0;
        for (int i = 0; i < 4; i++) {
            weightedWeeklyDemand += weeklyTotals[i] * WEEK_WEIGHTS[i];
        }
        double avgDailyDemand = weightedWeeklyDemand / 7.0;

        double predictedDemand = avgDailyDemand * forecastPeriodDays;

        double safetyStock = avgDailyDemand * Math.sqrt(Math.max(product.getLeadTimeDays(), 1));
        double reorderPoint = (avgDailyDemand * product.getLeadTimeDays()) + safetyStock;

        double recommendedOrderQty = Math.max(0, predictedDemand + safetyStock - product.getCurrentStock());

        double daysUntilStockout = avgDailyDemand > 0
                ? product.getCurrentStock() / avgDailyDemand
                : 999;

        DemandPrediction.RiskLevel risk;
        if (avgDailyDemand == 0) risk = DemandPrediction.RiskLevel.NONE;
        else if (daysUntilStockout <= 3) risk = DemandPrediction.RiskLevel.CRITICAL;
        else if (daysUntilStockout <= 7) risk = DemandPrediction.RiskLevel.HIGH;
        else if (daysUntilStockout <= 14) risk = DemandPrediction.RiskLevel.MEDIUM;
        else risk = DemandPrediction.RiskLevel.LOW;

        DemandPrediction.Trend trend = classifyTrend(weeklyTotals);

        DemandPrediction.Confidence confidence;
        if (totalHistoricalUnits >= 40) confidence = DemandPrediction.Confidence.HIGH;
        else if (totalHistoricalUnits >= 10) confidence = DemandPrediction.Confidence.MEDIUM;
        else confidence = DemandPrediction.Confidence.LOW;

        String modelUsed = totalHistoricalUnits > 0
                ? "Weighted Moving Average (4-week)"
                : "Baseline (insufficient historical data)";

        return DemandPrediction.builder()
                .product(product)
                .forecastPeriodDays(forecastPeriodDays)
                .avgDailyDemand(round(avgDailyDemand))
                .predictedDemand(round(predictedDemand))
                .reorderPoint(round(reorderPoint))
                .recommendedOrderQty(round(recommendedOrderQty))
                .daysUntilStockout(round(daysUntilStockout))
                .riskLevel(risk)
                .trend(trend)
                .confidence(confidence)
                .modelUsed(modelUsed)
                .business(product.getBusiness())
                .build();
    }

    /**
     * weeklyTotals[0] = most recent week ... weeklyTotals[3] = oldest week.
     */
    private DemandPrediction.Trend classifyTrend(double[] weeklyTotals) {
        double mostRecent = weeklyTotals[0];
        double oldest = weeklyTotals[3];
        double mean = (weeklyTotals[0] + weeklyTotals[1] + weeklyTotals[2] + weeklyTotals[3]) / 4.0;

        if (mean == 0) return DemandPrediction.Trend.STABLE;

        double variance = 0;
        for (double w : weeklyTotals) variance += Math.pow(w - mean, 2);
        variance /= 4.0;
        double stdDev = Math.sqrt(variance);
        double coefficientOfVariation = stdDev / mean;

        if (coefficientOfVariation > 0.5) return DemandPrediction.Trend.VOLATILE;

        double change = mean == 0 ? 0 : (mostRecent - oldest) / Math.max(oldest, 1);
        if (change > 0.15) return DemandPrediction.Trend.RISING;
        if (change < -0.15) return DemandPrediction.Trend.DECLINING;
        return DemandPrediction.Trend.STABLE;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private DemandPredictionResponse toResponse(DemandPrediction p) {
        return new DemandPredictionResponse(
                p.getId(), p.getProduct().getId(), p.getProduct().getName(),
                p.getForecastPeriodDays(), p.getAvgDailyDemand(), p.getPredictedDemand(),
                p.getReorderPoint(), p.getRecommendedOrderQty(), p.getDaysUntilStockout(),
                p.getRiskLevel().name(), p.getTrend().name(), p.getConfidence().name(),
                p.getModelUsed(), p.getGeneratedAt());
    }
}
