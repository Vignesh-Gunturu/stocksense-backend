package com.inventory.controller;

import com.inventory.service.DashboardService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<?> get() {
        return ApiResponse.ok(dashboardService.getDashboard(CurrentUser.businessId()));
    }
}
