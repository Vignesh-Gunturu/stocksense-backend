package com.inventory.controller;

import com.inventory.dto.request.CustomerRequest;
import com.inventory.service.CustomerService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(customerService.list(CurrentUser.businessId()));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CustomerRequest req) {
        return ApiResponse.ok("Customer created", customerService.create(CurrentUser.businessId(), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest req) {
        return ApiResponse.ok("Customer updated", customerService.update(CurrentUser.businessId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        customerService.delete(CurrentUser.businessId(), id);
        return ApiResponse.ok("Customer deleted", null);
    }
}
