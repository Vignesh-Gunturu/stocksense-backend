package com.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaleRequest {
    private Long customerId;
    private Double discount;
    private Double tax;
    private String paymentStatus;
    @NotEmpty private List<SaleItemRequest> items;
}
