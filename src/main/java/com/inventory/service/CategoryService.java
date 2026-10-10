package com.inventory.service;

import com.inventory.dto.request.CategoryRequest;
import com.inventory.dto.response.CategoryResponse;
import com.inventory.entity.Business;
import com.inventory.entity.Category;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.BusinessRepository;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;

    public CategoryResponse create(Long businessId, CategoryRequest req) {
        Business business = businessRepository.getReferenceById(businessId);
        Category category = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .business(business)
                .build();
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    public List<CategoryResponse> list(Long businessId) {
        return categoryRepository.findByBusinessId(businessId).stream().map(this::toResponse).toList();
    }

    public CategoryResponse update(Long businessId, Long id, CategoryRequest req) {
        Category category = getOwned(businessId, id);
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    public void delete(Long businessId, Long id) {
        Category category = getOwned(businessId, id);
        categoryRepository.delete(category);
    }

    private Category getOwned(Long businessId, Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getBusiness().getId().equals(businessId)) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private CategoryResponse toResponse(Category c) {
        long count = productRepository.findByBusinessId(c.getBusiness().getId()).stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
                .count();
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), count);
    }
}
