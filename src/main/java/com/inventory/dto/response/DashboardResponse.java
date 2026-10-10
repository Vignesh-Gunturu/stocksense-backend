package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data @AllArgsConstructor
public class DashboardResponse {
    private long totalProducts;
    private long totalStockUnits;
    private double inventoryValue;
    private double potentialSalesValue;
    private double todaysSales;
    private double monthlyRevenue;
    private double monthlyProfit;
    private long pendingPurchases;
    private long lowStockItems;
    private long outOfStockItems;
    private long overstockItems;
    private List<TopProduct> topProductsByRevenue;
    private List<Insight> insights;

    @Data @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String name;
        private double revenue;
        private int unitsSold;
    }

    @Data @AllArgsConstructor
    public static class Insight {
        private String type;
        private String message;
    }
}
