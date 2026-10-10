package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String address;
    private String taxId;
    private Integer leadTimeDays;
    private Double rating;
    private String status;
}
