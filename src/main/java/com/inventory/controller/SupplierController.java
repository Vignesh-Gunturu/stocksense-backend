package com.inventory.controller;

import com.inventory.dto.request.SupplierRequest;
import com.inventory.service.SupplierService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(supplierService.list(CurrentUser.businessId()));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody SupplierRequest req) {
        return ApiResponse.ok("Supplier created", supplierService.create(CurrentUser.businessId(), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody SupplierRequest req) {
        return ApiResponse.ok("Supplier updated", supplierService.update(CurrentUser.businessId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        supplierService.delete(CurrentUser.businessId(), id);
        return ApiResponse.ok("Supplier deleted", null);
    }
}
