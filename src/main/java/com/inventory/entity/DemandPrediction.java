package com.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demand_predictions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "forecast_period_days")
    private Integer forecastPeriodDays;

    @Column(name = "avg_daily_demand")
    private Double avgDailyDemand;

    @Column(name = "predicted_demand")
    private Double predictedDemand;

    @Column(name = "reorder_point")
    private Double reorderPoint;

    @Column(name = "recommended_order_qty")
    private Double recommendedOrderQty;

    @Column(name = "days_until_stockout")
    private Double daysUntilStockout;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    private Trend trend;

    @Column(name = "confidence")
    @Enumerated(EnumType.STRING)
    private Confidence confidence;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @PrePersist
    public void prePersist() {
        this.generatedAt = LocalDateTime.now();
    }

    public enum RiskLevel { CRITICAL, HIGH, MEDIUM, LOW, NONE }
    public enum Trend { RISING, STABLE, DECLINING, VOLATILE }
    public enum Confidence { HIGH, MEDIUM, LOW }
}
