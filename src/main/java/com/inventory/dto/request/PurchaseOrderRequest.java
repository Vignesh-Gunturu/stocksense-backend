package com.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {
    @NotNull private Long supplierId;
    private LocalDate expectedDelivery;
    @NotEmpty private List<PurchaseOrderItemRequest> items;
}
