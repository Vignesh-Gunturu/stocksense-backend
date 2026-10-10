package com.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "sale_date")
    private LocalDateTime saleDate;

    @Column(name = "discount")
    private Double discount = 0.0;

    @Column(name = "tax")
    private Double tax = 0.0;

    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @Column(name = "payment_status")
    private String paymentStatus = "PAID";

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @PrePersist
    public void prePersist() {
        if (this.saleDate == null) this.saleDate = LocalDateTime.now();
    }
}
