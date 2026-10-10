package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor
public class SaleResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private LocalDateTime saleDate;
    private Double discount;
    private Double tax;
    private Double totalAmount;
    private String paymentStatus;
    private List<Item> items;

    @Data @AllArgsConstructor
    public static class Item {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;
    }
}
