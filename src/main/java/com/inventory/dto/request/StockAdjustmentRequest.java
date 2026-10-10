package com.inventory.dto.request;

import com.inventory.entity.InventoryTransaction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
    @NotNull private Long productId;
    @NotNull private InventoryTransaction.MovementType type;
    @NotNull private Integer quantity;
    private String reason;
}
