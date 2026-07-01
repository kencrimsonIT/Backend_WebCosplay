package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.UpsertCategoryRequest;
import com.example.thuedocosplay.dto.response.CategoryResponse;
import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.CategoryRepository;
import com.example.thuedocosplay.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
    }

    @Transactional
    public CategoryResponse createCategory(UpsertCategoryRequest request) {
        if (categoryRepository.findByNameIgnoreCase(request.getName().trim()).isPresent()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpsertCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        categoryRepository.findByNameIgnoreCase(request.getName().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Tên danh mục đã tồn tại");
                    }
                });

        category.setName(request.getName().trim());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        if (productRepository.countByCategory(category) > 0) {
            throw new IllegalArgumentException("Không thể xóa danh mục đang có sản phẩm. Vui lòng xóa sản phẩm trước hoặc ẩn danh mục.");
        }

        categoryRepository.delete(category);
    }
}
