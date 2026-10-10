package com.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaleItemRequest {
    @NotNull private Long productId;
    @NotNull private Integer quantity;
    private Double price;
}
