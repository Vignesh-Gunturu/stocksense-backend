package com.inventory.controller;

import com.inventory.dto.request.StockAdjustmentRequest;
import com.inventory.service.InventoryService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/adjust")
    public ApiResponse<?> adjust(@Valid @RequestBody StockAdjustmentRequest req) {
        return ApiResponse.ok("Stock updated",
                inventoryService.adjustStock(CurrentUser.businessId(), CurrentUser.userId(), req));
    }

    @GetMapping("/movements")
    public ApiResponse<?> history() {
        return ApiResponse.ok(inventoryService.history(CurrentUser.businessId()));
    }

    @GetMapping("/movements/product/{productId}")
    public ApiResponse<?> historyForProduct(@PathVariable Long productId) {
        return ApiResponse.ok(inventoryService.historyForProduct(productId));
    }
}
