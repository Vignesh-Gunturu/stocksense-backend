package com.inventory.controller;

import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.RegisterBusinessRequest;
import com.inventory.service.AuthService;
import com.inventory.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterBusinessRequest req) {
        return ApiResponse.ok("Business registered successfully", authService.registerBusiness(req));
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Login successful", authService.login(req));
    }
}
