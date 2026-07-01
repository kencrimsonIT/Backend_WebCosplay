package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.ProductFilterRequest;
import com.example.thuedocosplay.dto.response.PagedResponse;
import com.example.thuedocosplay.dto.response.ProductResponse;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.spec.ProductSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(ProductFilterRequest filter) {

        Sort sort = switch (filter.getSort()) {
            case "oldest"      -> Sort.by("id").ascending();
            case "price_asc"   -> Sort.by("pricePerDay").ascending();
            case "price_desc"  -> Sort.by("pricePerDay").descending();
            // "newest" là default, "best_seller" xử lý riêng dưới
            default            -> Sort.by("id").descending();
        };
        if ("best_seller".equals(filter.getSort())) {
            return getBestSellerPaged(filter);
        }

        PageRequest pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(filter.getSize(), 50),
                sort
        );

        Page<ProductResponse> page = productRepository
                .findAll(ProductSpec.of(filter), pageable)
                .map(this::toResponse);

        return PagedResponse.of(page);
    }

    private PagedResponse<ProductResponse> getBestSellerPaged(ProductFilterRequest filter) {
        PageRequest pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(filter.getSize(), 50)
        );

        List<Product> products = productRepository.findTopBestSellerSimple(pageable);

        // Apply filter thủ công
        List<ProductResponse> filtered = products.stream()
                .filter(p -> filter.getCategoryId() == null
                        || p.getCategory().getId().equals(filter.getCategoryId()))
                .filter(p -> filter.getKeyword() == null || filter.getKeyword().isBlank()
                        || p.getName().toLowerCase().contains(filter.getKeyword().toLowerCase()))
                .map(this::toResponse)
                .toList();

        return PagedResponse.<ProductResponse>builder()
                .content(filtered)
                .page(filter.getPage())
                .size(filter.getSize())
                .totalElements(filtered.size())
                .totalPages(1)
                .last(true)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getNewestProducts(int limit) {
        return productRepository
                .findTopNewest(PageRequest.of(0, Math.min(limit, 20)))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getBestSellerProducts(int limit) {
        List<Product> results = productRepository
                .findTopBestSellerSimple(PageRequest.of(0, Math.min(limit, 20)));

        // Fallback nếu chưa có đơn hàng → trả về mới nhất
        if (results.isEmpty()) {
            results = productRepository.findTopNewest(PageRequest.of(0, Math.min(limit, 20)));
        }

        return results.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllVisibleProducts() {
        return productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getVisible()))
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse toResponse(Product product) {
        Double avg = product.getRating();
        Integer count = product.getReviewCount();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .description(product.getDescription())
                .pricePerDay(product.getPricePerDay())
                .deposit(product.getDeposit())
                .imageUrl(product.getImageUrl())
                .visible(product.getVisible())
                .avgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .reviewCount(count != null ? count.longValue() : 0L)
                .build();
    }
}