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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RentalOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(100);

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        boolean online = request.getPaymentMethod() == PaymentMethod.VNPAY
                || request.getPaymentMethod() == PaymentMethod.MOMO;

        OrderStatus initialStatus = online ? OrderStatus.PENDING_PAYMENT : OrderStatus.PENDING_CONFIRM;

        RentalOrder order = RentalOrder.builder()
                .orderCode(generateOrderCode())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
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

            // BẮT BUỘC ID SẢN PHẨM KHÔNG ĐƯỢC RỖNG
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Lỗi: Dữ liệu gửi lên bị thiếu productId (Kiểm tra lại tên biến trong DTO)");
            }

            // TÌM SẢN PHẨM, KHÔNG THẤY THÌ BÁO LỖI LUÔN, KHÔNG DÙNG orElse(null) NỮA
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + itemReq.getProductId()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product) // Chắc chắn lúc này product đã có dữ liệu
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

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return OrderMapper.toResponses(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return OrderMapper.toResponse(findOrder(id));
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
    public void markPaid(RentalOrder order) {
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public RentalOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

    private String generateOrderCode() {
        int year = Year.now().getValue();
        // Lấy 6 số cuối của thời gian hiện tại (tính bằng mili-giây)
        long randomSuffix = System.currentTimeMillis() % 1000000;
        return "ORD-" + year + "-" + String.format("%06d", randomSuffix);
    }
    // Lấy đơn hàng theo email user (dùng cho /my endpoint)
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // (Lưu ý: Nếu Repository của bạn chưa có hàm này, xem Bước 3)
        return orderRepository.findByCustomerOrEmail(user, email)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    // User hủy đơn — chỉ khi đơn ở trạng thái chờ
    @Transactional
    public OrderResponse cancelOrderByUser(Long orderId, String userEmail, String reason) {
        RentalOrder order = findOrder(orderId);

        // Kiểm tra quyền: chỉ chủ đơn mới được hủy
        boolean isOwner = userEmail.equals(order.getCustomerEmail())
                || (order.getCustomer() != null
                && userEmail.equals(order.getCustomer().getEmail()));
        if (!isOwner) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này");
        }

        // Chỉ được hủy khi chưa xác nhận hoặc chờ thanh toán
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT
                && order.getStatus() != OrderStatus.PENDING_CONFIRM) {
            throw new IllegalStateException(
                    "Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return OrderMapper.toResponse(orderRepository.save(order));
    }
}
