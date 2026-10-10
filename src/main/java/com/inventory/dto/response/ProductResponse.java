package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private String unit;
    private Double purchasePrice;
    private Double sellingPrice;
    private Double tax;
    private Integer minStock;
    private Integer maxStock;
    private Integer reorderPoint;
    private Integer leadTimeDays;
    private Integer currentStock;
    private String imageUrl;
    private String status;
    private String stockStatus;
}
