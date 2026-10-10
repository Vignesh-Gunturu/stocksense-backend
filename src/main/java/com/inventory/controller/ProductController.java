package com.inventory.controller;

import com.inventory.dto.request.ProductRequest;
import com.inventory.service.ProductService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(productService.list(CurrentUser.businessId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.get(CurrentUser.businessId(), id));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok("Product created", productService.create(CurrentUser.businessId(), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok("Product updated", productService.update(CurrentUser.businessId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        productService.delete(CurrentUser.businessId(), id);
        return ApiResponse.ok("Product deleted", null);
    }

    @GetMapping("/low-stock")
    public ApiResponse<?> lowStock() {
        return ApiResponse.ok(productService.lowStock(CurrentUser.businessId()));
    }

    @GetMapping("/out-of-stock")
    public ApiResponse<?> outOfStock() {
        return ApiResponse.ok(productService.outOfStock(CurrentUser.businessId()));
    }

    @GetMapping("/overstock")
    public ApiResponse<?> overstock() {
        return ApiResponse.ok(productService.overstock(CurrentUser.businessId()));
    }
}
