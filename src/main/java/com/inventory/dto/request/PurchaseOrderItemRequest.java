package com.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderItemRequest {
    @NotNull private Long productId;
    @NotNull private Integer quantity;
    private Double purchasePrice;
}
