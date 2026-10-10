package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class DemandPredictionResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer forecastPeriodDays;
    private Double avgDailyDemand;
    private Double predictedDemand;
    private Double reorderPoint;
    private Double recommendedOrderQty;
    private Double daysUntilStockout;
    private String riskLevel;
    private String trend;
    private String confidence;
    private String modelUsed;
    private LocalDateTime generatedAt;
}
