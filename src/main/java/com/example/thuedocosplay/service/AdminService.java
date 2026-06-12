package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.UpsertCategoryRequest;
import com.example.thuedocosplay.dto.request.UpsertProductRequest;
import com.example.thuedocosplay.dto.request.UpsertUserRequest;
import com.example.thuedocosplay.dto.response.CategoryResponse;
import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listUsers() {
        return userRepository.findAll().stream().map(this::userMap).toList();
    }

    @Transactional
    public Map<String, Object> createUser(UpsertUserRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();
        return userMap(userRepository.save(user));
    }

    @Transactional
    public Map<String, Object> updateUser(Long id, UpsertUserRequest request) {
        User user = findUser(id);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        return userMap(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(findUser(id));
    }

    @Transactional
    public Map<String, Object> toggleUser(Long id) {
        User user = findUser(id);
        user.setEnabled(!Boolean.TRUE.equals(user.isEnabled()));
        return userMap(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryService.getAllCategories();
    }

    @Transactional
    public CategoryResponse createCategory(UpsertCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpsertCategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryService.deleteCategory(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listProducts() {
        return productRepository.findAll().stream().map(this::productMap).toList();
    }

    @Transactional
    public Map<String, Object> createProduct(UpsertProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .category(findCategory(request.getCategoryId()))
                .pricePerDay(request.getPricePerDay())
                .deposit(request.getDeposit())
                .imageUrl(request.getImageUrl())
                .visible(request.getVisible() != null ? request.getVisible() : true)
                .build();
        return productMap(productRepository.save(product));
    }

    @Transactional
    public Map<String, Object> updateProduct(Long id, UpsertProductRequest request) {
        Product product = findProduct(id);
        product.setName(request.getName());
        product.setCategory(findCategory(request.getCategoryId()));
        product.setPricePerDay(request.getPricePerDay());
        product.setDeposit(request.getDeposit());
        product.setImageUrl(request.getImageUrl());
        if (request.getVisible() != null) {
            product.setVisible(request.getVisible());
        }
        return productMap(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findProduct(id));
    }

    @Transactional
    public Map<String, Object> toggleProductVisibility(Long id) {
        Product product = findProduct(id);
        product.setVisible(!Boolean.TRUE.equals(product.getVisible()));
        return productMap(productRepository.save(product));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private Category findCategory(Long id) {
        return categoryService.findById(id);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    private Map<String, Object> userMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole().name());
        map.put("enabled", user.isEnabled());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }

    private Map<String, Object> productMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("categoryId", product.getCategory().getId());
        map.put("categoryName", product.getCategory().getName());
        map.put("pricePerDay", product.getPricePerDay());
        map.put("deposit", product.getDeposit());
        map.put("imageUrl", product.getImageUrl());
        map.put("visible", product.getVisible());
        return map;
    }
}
