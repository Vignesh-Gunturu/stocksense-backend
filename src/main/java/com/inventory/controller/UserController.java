package com.inventory.controller;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.service.UserService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(userService.listUsers(CurrentUser.businessId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_ADMIN')")
    public ApiResponse<?> create(@Valid @RequestBody CreateUserRequest req) {
        return ApiResponse.ok("User created", userService.createUser(CurrentUser.businessId(), req));
    }
}
