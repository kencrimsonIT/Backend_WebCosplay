package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.CreateOnlinePaymentRequest;
import com.example.thuedocosplay.dto.response.PaymentResponse;
import com.example.thuedocosplay.entity.PaymentTransaction;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.entity.enums.PaymentMethod;
import com.example.thuedocosplay.entity.enums.PaymentStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final OrderService orderService;
    private final VnPayService vnPayService;
    private final MoMoService moMoService;

    // =========================================================================
    // 1. TẠO URL THANH TOÁN
    // =========================================================================
    @Transactional
    public PaymentResponse createOnlinePayment(CreateOnlinePaymentRequest request, String clientIp) {
        RentalOrder order = orderService.findOrder(request.getOrderId());

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        String txnRef = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        long amount = order.getGrandTotal().longValue();
        String orderInfo = "Thanh toan don hang " + order.getOrderCode();
        String paymentUrl = "";

        if (order.getPaymentMethod() == PaymentMethod.VNPAY) {
            paymentUrl = vnPayService.createPaymentUrl(txnRef, amount, orderInfo, clientIp);
        } else if (order.getPaymentMethod() == PaymentMethod.MOMO) {
            paymentUrl = moMoService.createPayment(txnRef, amount, orderInfo);
        } else {
            throw new IllegalArgumentException("Hệ thống chỉ hỗ trợ VNPAY và MOMO");
        }

        PaymentTransaction payment = PaymentTransaction.builder()
                .txnRef(txnRef)
                .order(order)
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .amount(order.getGrandTotal())
                .paymentUrl(paymentUrl)
                .build();

        paymentRepository.save(payment);

        return toResponse(payment);
    }

    // =========================================================================
    // 2. XỬ LÝ VNPAY
    // =========================================================================

    public Map<String, Object> handleVnPayReturn(Map<String, String> params) {
        boolean validSig = vnPayService.verifySignature(params);
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        if (!validSig) {
            return Map.of("success", false, "message", "Chữ ký không hợp lệ", "txnRef", txnRef != null ? txnRef : "");
        }

        return Map.of(
                "success", "00".equals(responseCode),
                "txnRef", txnRef,
                "responseCode", responseCode,
                "message", "00".equals(responseCode) ? "Giao dịch thành công" : "Giao dịch thất bại"
        );
    }

    @Transactional
    public Map<String, String> processVnPayIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!vnPayService.verifySignature(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Signature");
                return response;
            }

            String txnRef = params.get("vnp_TxnRef");
            String vnpAmount = params.get("vnp_Amount");
            String responseCode = params.get("vnp_ResponseCode");

            PaymentTransaction txn = paymentRepository.findByTxnRef(txnRef).orElse(null);

            if (txn == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }

            if (txn.getStatus() == PaymentStatus.SUCCESS || txn.getStatus() == PaymentStatus.FAILED) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            long expectedAmount = txn.getAmount().longValue() * 100;
            long receivedAmount = Long.parseLong(vnpAmount);
            if (expectedAmount != receivedAmount) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid amount");
                return response;
            }

            if ("00".equals(responseCode)) {
                txn.setStatus(PaymentStatus.SUCCESS);
                txn.setPaidAt(LocalDateTime.now());
                orderService.markPaid(txn.getOrder()); // Gọi logic cập nhật đơn hàng của bạn
                log.info("[VNPay] Thanh toán thành công cho đơn: {}", txnRef);
            } else {
                txn.setStatus(PaymentStatus.FAILED);
                txn.getOrder().setStatus(OrderStatus.CANCELLED);
                log.info("[VNPay] Thanh toán thất bại cho đơn: {}", txnRef);
            }

            paymentRepository.save(txn);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");

        } catch (Exception e) {
            log.error("[VNPay IPN] Lỗi xử lý", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }
        return response;
    }

    // =========================================================================
    // 3. XỬ LÝ MOMO
    // =========================================================================

    public Map<String, Object> handleMoMoReturn(Map<String, String> params) {
        int resultCode = Integer.parseInt(params.getOrDefault("resultCode", "-1"));
        String orderId = params.get("orderId");

        return Map.of(
                "success", resultCode == 0,
                "txnRef", orderId != null ? orderId : "",
                "message", params.getOrDefault("message", ""),
                "resultCode", resultCode
        );
    }

    @Transactional
    public Map<String, Object> processMoMoIpn(Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String partnerCode = str(body, "partnerCode");
            String orderId = str(body, "orderId");
            String requestId = str(body, "requestId");
            String amount = str(body, "amount");
            String orderInfo = str(body, "orderInfo");
            String orderType = str(body, "orderType");
            String transId = str(body, "transId");
            int resultCode = Integer.parseInt(str(body, "resultCode"));
            String message = str(body, "message");
            String payType = str(body, "payType");
            long responseTime = Long.parseLong(str(body, "responseTime"));
            String extraData = str(body, "extraData");
            String signature = str(body, "signature");

            boolean validSig = moMoService.verifyIpnSignature(
                    partnerCode, orderId, requestId, amount, orderInfo,
                    orderType, transId, resultCode, message, payType,
                    responseTime, extraData, signature);

            if (!validSig) {
                response.put("resultCode", 97);
                response.put("message", "Invalid signature");
                return response;
            }

            PaymentTransaction txn = paymentRepository.findByTxnRef(orderId).orElse(null);

            if (txn == null) {
                response.put("resultCode", 1);
                response.put("message", "Order not found");
                return response;
            }

            if (txn.getStatus() == PaymentStatus.SUCCESS || txn.getStatus() == PaymentStatus.FAILED) {
                response.put("resultCode", 0);
                response.put("message", "Already confirmed");
                return response;
            }

            if (resultCode == 0) {
                txn.setStatus(PaymentStatus.SUCCESS);
                txn.setPaidAt(LocalDateTime.now());
                orderService.markPaid(txn.getOrder());
                log.info("[MoMo] Thanh toán thành công cho đơn: {}", orderId);
            } else {
                txn.setStatus(PaymentStatus.FAILED);
                txn.getOrder().setStatus(OrderStatus.CANCELLED);
                log.info("[MoMo] Thanh toán thất bại cho đơn: {}", orderId);
            }

            paymentRepository.save(txn);
            response.put("resultCode", 0);
            response.put("message", "Success");

        } catch (Exception e) {
            log.error("[MoMo IPN] Lỗi xử lý", e);
            response.put("resultCode", 99);
            response.put("message", "Unknown error");
        }
        return response;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    @Transactional(readOnly = true)
    public PaymentResponse getByTxnRef(String txnRef) {
        PaymentTransaction payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán"));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(PaymentTransaction payment) {
        return PaymentResponse.builder()
                .txnRef(payment.getTxnRef())
                .orderId(payment.getOrder().getId())
                .orderCode(payment.getOrder().getOrderCode())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paymentUrl(payment.getPaymentUrl())
                .build();
    }

    // Hàm phụ trợ cho MoMo IPN
    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "0";
    }
}