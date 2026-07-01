package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.CreateOrderItemRequest;
import com.example.thuedocosplay.dto.request.CreateOrderRequest;
import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.entity.OrderItem;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.entity.enums.PaymentMethod;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final RentalOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        return createOrder(request, null);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String currentUserEmail) {
        boolean online = request.getPaymentMethod() == PaymentMethod.VNPAY
                || request.getPaymentMethod() == PaymentMethod.MOMO;
        OrderStatus initialStatus = online ? OrderStatus.PENDING_PAYMENT : OrderStatus.PENDING_CONFIRM;
        User customer = resolveOrderCustomer(currentUserEmail, request.getCustomerEmail());

        RentalOrder order = RentalOrder.builder()
                .orderCode(generateOrderCode())
                .customer(customer)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(resolveCustomerEmail(customer, request.getCustomerEmail()))
                .shippingAddress(request.getShippingAddress())
                .status(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .rentalTotal(request.getRentalTotal())
                .warrantyTotal(request.getWarrantyTotal())
                .depositTotal(request.getDepositTotal())
                .grandTotal(request.getGrandTotal())
                .rentFrom(request.getRentFrom())
                .rentTo(request.getRentTo())
                .build();

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Du lieu don hang thieu productId");
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay san pham voi ID: " + itemReq.getProductId()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(itemReq.getProductName())
                    .categoryName(itemReq.getCategoryName())
                    .size(itemReq.getSize())
                    .days(itemReq.getDays())
                    .quantity(itemReq.getQuantity())
                    .lineTotal(itemReq.getLineTotal())
                    .build();
            order.getItems().add(item);
        }

        if (!online) {
            order.setPaidAt(LocalDateTime.now());
        }

        RentalOrder saved = orderRepository.save(order);
        log.info(
                "[Order] Created orderCode={} customerId={} customerEmail={} itemCount={} rentalTotal={} depositTotal={} warrantyTotal={} grandTotal={} paymentMethod={} status={}",
                saved.getOrderCode(),
                saved.getCustomer() != null ? saved.getCustomer().getId() : null,
                saved.getCustomerEmail(),
                saved.getItems().size(),
                saved.getRentalTotal(),
                saved.getDepositTotal(),
                saved.getWarrantyTotal(),
                saved.getGrandTotal(),
                saved.getPaymentMethod(),
                saved.getStatus()
        );

        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return OrderMapper.toResponses(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return OrderMapper.toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrderHistory(String currentUserEmail, OrderStatus status, LocalDate fromDate, LocalDate toDate) {
        User user = requireCurrentUser(currentUserEmail);
        validateDateRange(fromDate, toDate);

        List<RentalOrder> orders = orderRepository.findCustomerHistory(
                user.getId(),
                user.getEmail(),
                status,
                fromDate,
                toDate
        );
        BigDecimal totalGrand = orders.stream()
                .map(RentalOrder::getGrandTotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info(
                "[OrderHistory] Listed customerId={} email={} status={} fromDate={} toDate={} orderCount={} totalGrand={}",
                user.getId(),
                user.getEmail(),
                status,
                fromDate,
                toDate,
                orders.size(),
                totalGrand
        );

        return OrderMapper.toResponses(orders);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(String currentUserEmail, Long orderId) {
        User user = requireCurrentUser(currentUserEmail);
        RentalOrder order = orderRepository.findCustomerOrderDetail(orderId, user.getId(), user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don hang trong lich su mua cua ban"));

        log.info(
                "[OrderHistory] Viewed detail customerId={} email={} orderId={} orderCode={} grandTotal={} status={} itemCount={}",
                user.getId(),
                user.getEmail(),
                order.getId(),
                order.getOrderCode(),
                order.getGrandTotal(),
                order.getStatus(),
                order.getItems().size()
        );

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        RentalOrder order = findOrder(id);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED && order.getPaidAt() == null) {
            order.setPaidAt(LocalDateTime.now());
        }
        RentalOrder saved = orderRepository.save(order);
        log.info("[Order] Updated status orderId={} orderCode={} oldStatus={} newStatus={}",
                saved.getId(), saved.getOrderCode(), oldStatus, saved.getStatus());
        return OrderMapper.toResponse(saved);
    }

    @Transactional
    public void markPaid(RentalOrder order) {
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("[Order] Marked paid orderId={} orderCode={} grandTotal={} paidAt={}",
                order.getId(), order.getOrderCode(), order.getGrandTotal(), order.getPaidAt());
    }

    public RentalOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don hang"));
    }

    private User requireCurrentUser(String currentUserEmail) {
        if (!hasCurrentUserEmail(currentUserEmail)) {
            log.warn("[OrderHistory] Rejected unauthenticated request");
            throw new AuthenticationCredentialsNotFoundException("Vui long dang nhap de xem lich su mua");
        }
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tai khoan dang dang nhap"));
    }

    private User resolveOrderCustomer(String currentUserEmail, String requestEmail) {
        if (hasCurrentUserEmail(currentUserEmail)) {
            return userRepository.findByEmail(currentUserEmail).orElse(null);
        }
        if (requestEmail != null && !requestEmail.isBlank()) {
            return userRepository.findByEmail(requestEmail).orElse(null);
        }
        return null;
    }

    private String resolveCustomerEmail(User customer, String requestEmail) {
        if (customer != null) {
            return customer.getEmail();
        }
        return requestEmail;
    }

    private boolean hasCurrentUserEmail(String currentUserEmail) {
        return currentUserEmail != null
                && !currentUserEmail.isBlank()
                && !"anonymousUser".equals(currentUserEmail);
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngay bat dau khong duoc sau ngay ket thuc");
        }
    }

    private String generateOrderCode() {
        int year = Year.now().getValue();
        long randomSuffix = System.currentTimeMillis() % 1000000;
        return "ORD-" + year + "-" + String.format("%06d", randomSuffix);
    }
}
