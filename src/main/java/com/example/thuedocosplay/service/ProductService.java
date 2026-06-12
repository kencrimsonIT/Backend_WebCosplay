package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.ProductResponse;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllVisibleProducts() {
        return productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getVisible()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .pricePerDay(product.getPricePerDay())
                .deposit(product.getDeposit())
                .imageUrl(product.getImageUrl())
                .visible(product.getVisible())
                .rating(4.5) // Placeholder
                .reviewCount(100) // Placeholder
                .build();
    }
}
