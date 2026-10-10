package com.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank private String sku;
    private String barcode;
    @NotBlank private String name;
    private String description;
    private Long categoryId;
    private Long supplierId;
    private String unit;
    @NotNull private Double purchasePrice;
    @NotNull private Double sellingPrice;
    private Double tax;
    private Integer minStock;
    private Integer maxStock;
    private Integer reorderPoint;
    private Integer leadTimeDays;
    private Integer currentStock;
    private String imageUrl;
    private String status;
}
