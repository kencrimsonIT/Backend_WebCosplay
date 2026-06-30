package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.ProductResponse;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.enums.ReviewStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;
    private final ProductReviewService reviewService;

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
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .description(product.getDescription())
                .pricePerDay(product.getPricePerDay())
                .deposit(product.getDeposit())
                .imageUrl(product.getImageUrl())
                .visible(product.getVisible())
                .quantity(product.getQuantity())
                .inventoryStatus(product.getInventoryStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .rating(roundOne(reviewRepository.averageRatingByProduct(product.getId(), ReviewStatus.VISIBLE)))
                .reviewCount((int) reviewRepository.countByProduct_IdAndStatus(product.getId(), ReviewStatus.VISIBLE))
                .reviews(reviewService.listVisibleProductReviews(product.getId()))
                .build();
    }

    private double roundOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
