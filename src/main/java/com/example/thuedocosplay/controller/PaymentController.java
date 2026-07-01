package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CreateOnlinePaymentRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.PaymentResponse;
import com.example.thuedocosplay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/online/create")
    public ApiResponse<PaymentResponse> createOnline(
            @Valid @RequestBody CreateOnlinePaymentRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        return ApiResponse.ok(paymentService.createOnlinePayment(request, clientIp));
    }
    @GetMapping("/vnpay/return")
    public void vnPayReturn(@RequestParam Map<String, String> params, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        paymentService.handleVnPayReturn(params);

        response.sendRedirect("http://localhost:3000/orders");
    }
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnPayIpn(@RequestParam Map<String, String> params) {
        System.out.println("========== VNPAY ĐÃ GỌI VÀO IPN: " + params.get("vnp_TxnRef") + " ==========");
        return ResponseEntity.ok(paymentService.processVnPayIpn(params));
    }

    @GetMapping("/momo/return")
    public ResponseEntity<?> moMoReturn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(paymentService.handleMoMoReturn(params));
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Map<String, Object>> moMoIpn(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(paymentService.processMoMoIpn(body));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip.split(",")[0].trim();
    }
}