package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.CreateOrderItemRequest;
import com.example.thuedocosplay.dto.request.CreateOrderRequest;
import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.entity.*;
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

    // ─────────────────────────────────────────────────────────────────────────
    // TẠO ĐƠN HÀNG
    // ─────────────────────────────────────────────────────────────────────────

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
                throw new IllegalArgumentException("Dữ liệu đơn hàng thiếu productId");
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + itemReq.getProductId()));

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
        log.info("[Order] Created orderCode={} customerEmail={} status={}", saved.getOrderCode(), saved.getCustomerEmail(), saved.getStatus());

        return OrderMapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUẢN LÝ ĐƠN HÀNG (Lịch sử, Chi tiết, Cập nhật, Hủy)
    // ─────────────────────────────────────────────────────────────────────────

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
        List<RentalOrder> orders = orderRepository.findCustomerHistory(user.getId(), user.getEmail(), status, fromDate, toDate);
        return OrderMapper.toResponses(orders);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(String currentUserEmail, Long orderId) {
        User user = requireCurrentUser(currentUserEmail);
        RentalOrder order = orderRepository.findCustomerOrderDetail(orderId, user.getId(), user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return orderRepository.findByCustomerOrEmail(user, email).stream().map(OrderMapper::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        RentalOrder order = findOrder(id);
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED && order.getPaidAt() == null) {
            order.setPaidAt(LocalDateTime.now());
        }
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrderByUser(Long orderId, String userEmail, String reason) {
        RentalOrder order = findOrder(orderId);
        boolean isOwner = userEmail.equals(order.getCustomerEmail())
                || (order.getCustomer() != null && userEmail.equals(order.getCustomer().getEmail()));
        
        if (!isOwner) throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này");
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.PENDING_CONFIRM) {
            throw new IllegalStateException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        log.info("[Order] Cancelled orderId={} reason={}", orderId, reason);
        return OrderMapper.toResponse(orderRepository.save(order));
    }
    @Transactional
    public void markPaid(RentalOrder order) {
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("[Order] Marked paid orderId={} orderCode={} grandTotal={} paidAt={}",
                order.getId(), order.getOrderCode(), order.getGrandTotal(), order.getPaidAt());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CÁC HÀM HỖ TRỢ
    // ─────────────────────────────────────────────────────────────────────────

    public RentalOrder findOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

    private User requireCurrentUser(String currentUserEmail) {
        if (!hasCurrentUserEmail(currentUserEmail)) throw new AuthenticationCredentialsNotFoundException("Vui lòng đăng nhập");
        return userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    private User resolveOrderCustomer(String currentUserEmail, String requestEmail) {
        if (hasCurrentUserEmail(currentUserEmail)) return userRepository.findByEmail(currentUserEmail).orElse(null);
        return (requestEmail != null && !requestEmail.isBlank()) ? userRepository.findByEmail(requestEmail).orElse(null) : null;
    }

    private String resolveCustomerEmail(User customer, String requestEmail) {
        return (customer != null) ? customer.getEmail() : requestEmail;
    }

    private boolean hasCurrentUserEmail(String currentUserEmail) {
        return currentUserEmail != null && !currentUserEmail.isBlank() && !"anonymousUser".equals(currentUserEmail);
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }
    }

    private String generateOrderCode() {
        return "ORD-" + Year.now().getValue() + "-" + String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}