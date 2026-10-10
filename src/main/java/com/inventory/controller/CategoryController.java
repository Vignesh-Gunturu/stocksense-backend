package com.inventory.controller;

import com.inventory.dto.request.CategoryRequest;
import com.inventory.service.CategoryService;
import com.inventory.util.ApiResponse;
import com.inventory.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(categoryService.list(CurrentUser.businessId()));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CategoryRequest req) {
        return ApiResponse.ok("Category created", categoryService.create(CurrentUser.businessId(), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.ok("Category updated", categoryService.update(CurrentUser.businessId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        categoryService.delete(CurrentUser.businessId(), id);
        return ApiResponse.ok("Category deleted", null);
    }
}
