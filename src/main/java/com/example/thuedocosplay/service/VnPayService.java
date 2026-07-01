package com.example.thuedocosplay.service;

import com.example.thuedocosplay.config.VnPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service xử lý tích hợp VNPay.
 *
 * Luồng:
 * 1. createPaymentUrl()  → sinh URL redirect người dùng đến VNPay
 * 2. verifyReturn()      → xác minh chữ ký khi VNPay redirect về frontend (return URL)
 * 3. verifyIpn()         → xác minh chữ ký IPN gọi từ server VNPay (backend-to-backend)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig config;

    // ─────────────────────────────────────────────────────────────────────────
    // Tạo URL thanh toán
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param txnRef    Mã giao dịch nội bộ (unique, tối đa 100 ký tự)
     * @param amount    Số tiền VND (VNPay nhân 100 — truyền vào đây số nguyên VND)
     * @param orderInfo Mô tả đơn hàng
     * @param clientIp  IP của người dùng
     */
    public String createPaymentUrl(String txnRef, long amount, String orderInfo, String clientIp) {
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String expireDate = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // +15 phút

        Map<String, String> params = new TreeMap<>(); // TreeMap → tự động sort theo key
        params.put("vnp_Version",    config.getVersion());
        params.put("vnp_Command",    config.getCommand());
        params.put("vnp_TmnCode",    config.getTmnCode());
        params.put("vnp_Amount",     String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        params.put("vnp_CurrCode",   config.getCurrCode());
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  config.getOrderType());
        params.put("vnp_Locale",     config.getLocale());
        params.put("vnp_ReturnUrl",  config.getReturnUrl());
        params.put("vnp_IpAddr",     clientIp);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String queryString = buildQueryString(params);
        String secureHash  = hmacSHA512(config.getHashSecret(), queryString);

        return config.getPaymentUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Xác minh chữ ký (dùng cho cả return URL và IPN)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Trả về true nếu chữ ký hợp lệ.
     * @param params Toàn bộ query params nhận được từ VNPay (Map<String, String>)
     */
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // Loại bỏ các field không tham gia ký
        Map<String, String> signParams = new TreeMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        String queryString   = buildQueryString(signParams);
        String computedHash  = hmacSHA512(config.getHashSecret(), queryString);

        return computedHash.equalsIgnoreCase(receivedHash);
    }

    /**
     * Kiểm tra giao dịch thành công: chữ ký hợp lệ VÀ mã phản hồi là "00".
     */
    public boolean isPaymentSuccessful(Map<String, String> params) {
        return verifySignature(params) && "00".equals(params.get("vnp_ResponseCode"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(URLEncoder.encode(entry.getKey(),   StandardCharsets.US_ASCII));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC-SHA512", e);
        }
    }
}
