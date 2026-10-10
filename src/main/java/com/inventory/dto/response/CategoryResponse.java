package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private long productCount;
}
