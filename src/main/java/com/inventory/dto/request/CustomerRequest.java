package com.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank private String name;
    private String email;
    private String phone;
    private String address;
    private String customerType;
}
