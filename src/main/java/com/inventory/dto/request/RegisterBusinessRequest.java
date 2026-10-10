package com.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterBusinessRequest {
    @NotBlank private String businessName;
    @NotBlank private String adminName;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6) private String password;
}
