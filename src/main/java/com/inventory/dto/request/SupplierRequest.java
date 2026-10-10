package com.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {
    @NotBlank private String name;
    private String company;
    private String email;
    private String phone;
    private String address;
    private String taxId;
    private Integer leadTimeDays;
    private Double rating;
    private String status;
}
