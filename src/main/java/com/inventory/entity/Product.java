package com.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    private String barcode;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String unit = "pcs";

    @Column(name = "purchase_price")
    private Double purchasePrice = 0.0;

    @Column(name = "selling_price")
    private Double sellingPrice = 0.0;

    private Double tax = 0.0;

    @Column(name = "min_stock")
    private Integer minStock = 0;

    @Column(name = "max_stock")
    private Integer maxStock = 1000;

    @Column(name = "reorder_point")
    private Integer reorderPoint = 10;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays = 7;

    @Column(name = "current_stock")
    private Integer currentStock = 0;

    private String imageUrl;

    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
