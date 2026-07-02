package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.CartItemRequest;
import com.example.thuedocosplay.dto.request.CartQuantityRequest;
import com.example.thuedocosplay.dto.response.CartItemResponse;
import com.example.thuedocosplay.dto.response.CartResponse;
import com.example.thuedocosplay.entity.CartItem;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.CartItemRepository;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User customer = requireUser(email);
        return toCartResponse(cartItemRepository.findAllByCustomer_IdOrderByUpdatedAtDesc(customer.getId()));
    }

    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        User customer = requireUser(email);
        Product product = requireAvailableProduct(request.getProductId());
        int requestedQty = normalizeQuantity(request.getQuantity());
        int maxQuantity = maxQuantity(product);

        CartItem item = cartItemRepository
                .findMatchingItem(
                        customer.getId(),
                        product.getId(),
                        blankToNull(request.getSize()),
                        request.getStartDate(),
                        request.getEndDate(),
                        normalizeWarranty(request.getWarranty())
                )
                .orElseGet(() -> CartItem.builder()
                        .customer(customer)
                        .product(product)
                        .size(blankToNull(request.getSize()))
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .days(request.getDays())
                        .quantity(0)
                        .warranty(normalizeWarranty(request.getWarranty()))
                        .warrantyFee(nullToZero(request.getWarrantyFee()))
                        .build());

        item.setQuantity(Math.min(item.getQuantity() + requestedQty, maxQuantity));
        item.setDays(request.getDays());
        item.setWarrantyFee(nullToZero(request.getWarrantyFee()));
        cartItemRepository.save(item);
        return getCart(email);
    }

    @Transactional
    public CartResponse updateQuantity(String email, Long itemId, CartQuantityRequest request) {
        User customer = requireUser(email);
        CartItem item = cartItemRepository.findByIdAndCustomer_Id(itemId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay san pham trong gio hang"));
        int maxQuantity = maxQuantity(item.getProduct());
        item.setQuantity(Math.min(normalizeQuantity(request.getQuantity()), maxQuantity));
        cartItemRepository.save(item);
        return getCart(email);
    }

    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        User customer = requireUser(email);
        CartItem item = cartItemRepository.findByIdAndCustomer_Id(itemId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay san pham trong gio hang"));
        cartItemRepository.delete(item);
        return getCart(email);
    }

    @Transactional
    public CartResponse clearCart(String email) {
        User customer = requireUser(email);
        cartItemRepository.deleteAllByCustomer_Id(customer.getId());
        return getCart(email);
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> responses = items.stream().map(this::toResponse).toList();
        BigDecimal rentalTotal = BigDecimal.ZERO;
        BigDecimal warrantyTotal = BigDecimal.ZERO;
        BigDecimal depositTotal = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItemResponse item : responses) {
            int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
            rentalTotal = rentalTotal.add(item.getRentalPrice().multiply(BigDecimal.valueOf(quantity)));
            warrantyTotal = warrantyTotal.add(item.getWarrantyFee().multiply(BigDecimal.valueOf(quantity)));
            depositTotal = depositTotal.add(item.getDeposit().multiply(BigDecimal.valueOf(quantity)));
            itemCount += quantity;
        }

        return CartResponse.builder()
                .items(responses)
                .itemCount(itemCount)
                .rentalTotal(rentalTotal)
                .warrantyTotal(warrantyTotal)
                .depositTotal(depositTotal)
                .total(rentalTotal.add(warrantyTotal).add(depositTotal))
                .build();
    }

    private CartItemResponse toResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal pricePerDay = nullToZero(product.getPricePerDay());
        BigDecimal rentalPrice = pricePerDay.multiply(BigDecimal.valueOf(item.getDays()));

        return CartItemResponse.builder()
                .id(item.getId())
                .cartKey(String.valueOf(item.getId()))
                .productId(product.getId())
                .name(product.getName())
                .image(product.getImageUrl())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .size(item.getSize())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .days(item.getDays())
                .pricePerDay(pricePerDay)
                .rentalPrice(rentalPrice)
                .deposit(nullToZero(product.getDeposit()))
                .warranty(item.getWarranty())
                .warrantyFee(nullToZero(item.getWarrantyFee()))
                .quantity(Math.min(item.getQuantity(), maxQuantity(product)))
                .maxQuantity(maxQuantity(product))
                .build();
    }

    private Product requireAvailableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay san pham"));
        if (!Boolean.TRUE.equals(product.getVisible())
                || product.getInventoryStatus() != ProductInventoryStatus.AVAILABLE
                || maxQuantity(product) <= 0) {
            throw new IllegalArgumentException("San pham khong kha dung de them vao gio hang");
        }
        return product;
    }

    private User requireUser(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Vui long dang nhap");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));
    }

    private int normalizeQuantity(Integer quantity) {
        return Math.max(1, quantity == null ? 1 : quantity);
    }

    private int maxQuantity(Product product) {
        return Math.max(0, product.getQuantity() == null ? 0 : product.getQuantity());
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeWarranty(String warranty) {
        return warranty == null || warranty.isBlank() ? "none" : warranty.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
