package com.inventory.controller;

import com.inventory.dto.request.SaleRequest;
import com.inventory.service.SaleService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(saleService.list(CurrentUser.businessId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        return ApiResponse.ok(saleService.get(CurrentUser.businessId(), id));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody SaleRequest req) {
        return ApiResponse.ok("Sale recorded",
                saleService.create(CurrentUser.businessId(), CurrentUser.userId(), req));
    }
}
