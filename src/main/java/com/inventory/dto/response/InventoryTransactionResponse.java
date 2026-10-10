package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class InventoryTransactionResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String type;
    private Integer quantity;
    private String reason;
    private String userName;
    private LocalDateTime createdAt;
}
