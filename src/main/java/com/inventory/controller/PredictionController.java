package com.inventory.controller;

import com.inventory.service.PredictionService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping
    public ApiResponse<?> latest() {
        return ApiResponse.ok(predictionService.latestForBusiness(CurrentUser.businessId()));
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<?> latestForProduct(@PathVariable Long productId) {
        return ApiResponse.ok(predictionService.latestForProduct(productId).orElse(null));
    }

    @PostMapping("/generate")
    public ApiResponse<?> generateAll(@RequestParam(defaultValue = "30") int forecastDays) {
        return ApiResponse.ok("Predictions generated",
                predictionService.generateForBusiness(CurrentUser.businessId(), forecastDays));
    }

    @PostMapping("/generate/{productId}")
    public ApiResponse<?> generateForProduct(@PathVariable Long productId,
                                              @RequestParam(defaultValue = "30") int forecastDays) {
        return ApiResponse.ok("Prediction generated",
                predictionService.generateForProduct(CurrentUser.businessId(), productId, forecastDays));
    }
}
