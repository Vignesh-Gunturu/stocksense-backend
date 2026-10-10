package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private String status;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private Double totalAmount;
    private List<Item> items;
    private LocalDateTime createdAt;

    @Data @AllArgsConstructor
    public static class Item {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private Integer quantityReceived;
        private Double purchasePrice;
    }
}
