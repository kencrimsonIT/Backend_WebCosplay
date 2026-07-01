package com.example.thuedocosplay.service;

import com.example.thuedocosplay.config.MoMoConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Service xử lý tích hợp MoMo (v2 - payWithMethod).
 *
 * Luồng:
 * 1. createPayment()   → gọi API MoMo, trả về payUrl để redirect người dùng
 * 2. verifyIpn()       → xác minh chữ ký IPN từ MoMo server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService {

    private final MoMoConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // Tạo yêu cầu thanh toán
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param txnRef    Mã giao dịch nội bộ (orderId gửi sang MoMo, unique)
     * @param amount    Số tiền VND
     * @param orderInfo Mô tả đơn hàng
     * @return payUrl để redirect người dùng
     */
    public String createPayment(String txnRef, long amount, String orderInfo) {
        try {
            String requestId  = config.getPartnerCode() + System.currentTimeMillis();
            String extraData  = "";
            String amountStr  = String.valueOf(amount);

            // Chuỗi ký theo thứ tự chuẩn MoMo v2
            String rawHash = "accessKey="   + config.getAccessKey()
                    + "&amount="            + amountStr
                    + "&extraData="         + extraData
                    + "&ipnUrl="            + config.getIpnUrl()
                    + "&orderId="           + txnRef
                    + "&orderInfo="         + orderInfo
                    + "&partnerCode="       + config.getPartnerCode()
                    + "&redirectUrl="       + config.getReturnUrl()
                    + "&requestId="         + requestId
                    + "&requestType="       + config.getRequestType();

            String signature = hmacSHA256(config.getSecretKey(), rawHash);

            // Build JSON body
            var body = objectMapper.createObjectNode();
            body.put("partnerCode",  config.getPartnerCode());
            body.put("partnerName",  "CosplayStar");
            body.put("storeId",      config.getPartnerCode());
            body.put("requestId",    requestId);
            body.put("amount",       amountStr);
            body.put("orderId",      txnRef);
            body.put("orderInfo",    orderInfo);
            body.put("redirectUrl",  config.getReturnUrl());
            body.put("ipnUrl",       config.getIpnUrl());
            body.put("lang",         "vi");
            body.put("extraData",    extraData);
            body.put("requestType",  config.getRequestType());
            body.put("signature",    signature);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

            log.info("[MoMo] Response: {}", response.body());

            int resultCode = json.path("resultCode").asInt(-1);
            if (resultCode != 0) {
                throw new RuntimeException("MoMo từ chối tạo giao dịch: " + json.path("message").asText());
            }

            return json.path("payUrl").asText();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kết nối MoMo: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Xác minh chữ ký IPN từ MoMo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * MoMo gửi IPN dạng JSON body tới ipnUrl.
     * @param partnerCode  Từ request body
     * @param orderId      Từ request body (= txnRef của mình)
     * @param requestId    Từ request body
     * @param amount       Từ request body
     * @param orderInfo    Từ request body
     * @param orderType    Từ request body
     * @param transId      Từ request body
     * @param resultCode   Từ request body
     * @param message      Từ request body
     * @param payType      Từ request body
     * @param responseTime Từ request body
     * @param extraData    Từ request body
     * @param signature    Từ request body (chữ ký cần verify)
     */
    public boolean verifyIpnSignature(
            String partnerCode, String orderId, String requestId,
            String amount, String orderInfo, String orderType,
            String transId, int resultCode, String message,
            String payType, long responseTime, String extraData,
            String signature) {

        String rawHash = "accessKey="    + config.getAccessKey()
                + "&amount="            + amount
                + "&extraData="         + extraData
                + "&message="           + message
                + "&orderId="           + orderId
                + "&orderInfo="         + orderInfo
                + "&orderType="         + orderType
                + "&partnerCode="       + partnerCode
                + "&payType="           + payType
                + "&requestId="         + requestId
                + "&responseTime="      + responseTime
                + "&resultCode="        + resultCode
                + "&transId="           + transId;

        String computed = hmacSHA256(config.getSecretKey(), rawHash);
        return computed.equalsIgnoreCase(signature);
    }

    public boolean isPaymentSuccessful(int resultCode) {
        return resultCode == 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    public static String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC-SHA256", e);
        }
    }
}
