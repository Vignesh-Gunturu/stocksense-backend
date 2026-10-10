package com.inventory.controller;

import com.inventory.dto.request.PurchaseOrderRequest;
import com.inventory.dto.request.ReceivePurchaseRequest;
import com.inventory.service.PurchaseService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(purchaseService.list(CurrentUser.businessId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        return ApiResponse.ok(purchaseService.get(CurrentUser.businessId(), id));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody PurchaseOrderRequest req) {
        return ApiResponse.ok("Purchase order created", purchaseService.create(CurrentUser.businessId(), req));
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<?> receive(@PathVariable Long id, @Valid @RequestBody ReceivePurchaseRequest req) {
        return ApiResponse.ok("Stock received",
                purchaseService.receive(CurrentUser.businessId(), CurrentUser.userId(), id, req));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long id) {
        purchaseService.cancel(CurrentUser.businessId(), id);
        return ApiResponse.ok("Purchase order cancelled", null);
    }
}
