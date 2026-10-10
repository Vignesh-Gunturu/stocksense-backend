package com.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReceivePurchaseRequest {
    @NotEmpty private List<ReceiveItem> items;

    @Data
    public static class ReceiveItem {
        private Long purchaseOrderItemId;
        private Integer quantityReceived;
    }
}
