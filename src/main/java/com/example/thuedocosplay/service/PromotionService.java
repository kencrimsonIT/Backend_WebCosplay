package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.UpsertPromotionRequest;
import com.example.thuedocosplay.dto.request.UpsertVoucherRequest;
import com.example.thuedocosplay.dto.response.PromotionResponse;
import com.example.thuedocosplay.dto.response.VoucherResponse;
import com.example.thuedocosplay.entity.Voucher;
import com.example.thuedocosplay.entity.enums.PromotionType;
import com.example.thuedocosplay.entity.enums.VoucherAudience;
import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import com.example.thuedocosplay.entity.enums.VoucherProductScope;
import com.example.thuedocosplay.entity.enums.VoucherStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> listBySeller(String email) {
        return voucherService.listSellerVouchers(email).stream()
                .map(this::toPromotionResponse)
                .toList();
    }

    @Transactional
    public PromotionResponse create(String email, UpsertPromotionRequest req) {
        VoucherResponse voucher = voucherService.createSellerVoucher(email, toVoucherRequest(req));
        return toPromotionResponse(voucher);
    }

    @Transactional
    public PromotionResponse update(String email, Long id, UpsertPromotionRequest req) {
        VoucherResponse voucher = voucherService.updateSellerVoucher(email, id, toVoucherRequest(req));
        return toPromotionResponse(voucher);
    }

    @Transactional
    public PromotionResponse toggleStatus(String email, Long id) {
        return toPromotionResponse(voucherService.toggleSellerVoucher(email, id));
    }

    @Transactional
    public PromotionResponse duplicate(String email, Long id) {
        return toPromotionResponse(voucherService.duplicateSellerVoucher(email, id));
    }

    @Transactional
    public void delete(String email, Long id) {
        voucherService.deleteSellerVoucher(email, id);
    }

    @Transactional(readOnly = true)
    public PromotionSummary getSummary(String email) {
        VoucherService.VoucherSummary s = voucherService.getSellerSummary(email);
        return new PromotionSummary(s.active(), s.totalUsed(), s.expiringSoon());
    }

    @Transactional(readOnly = true)
    public PromotionResponse checkAndApplyPromotion(String code, BigDecimal cartTotal) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
        validateSimpleVoucher(voucher, cartTotal);
        return PromotionResponse.fromVoucher(voucher);
    }

    @Transactional
    public void incrementUsedCount(String code) {
        if (code == null || code.isBlank()) return;
        voucherRepository.findByCodeIgnoreCase(normalizeCode(code)).ifPresent(voucher -> {
            voucher.setUsedCount((voucher.getUsedCount() == null ? 0 : voucher.getUsedCount()) + 1);
            voucherRepository.save(voucher);
        });
    }

    private UpsertVoucherRequest toVoucherRequest(UpsertPromotionRequest req) {
        UpsertVoucherRequest voucher = new UpsertVoucherRequest();
        voucher.setTitle(req.getTitle());
        voucher.setCode(normalizeCode(req.getCode()));
        voucher.setDiscountType(toVoucherDiscountType(req.getType()));
        voucher.setDiscountValue(req.getValue());
        voucher.setMinimumOrderAmount(req.getMinOrderAmount() == null ? BigDecimal.ZERO : req.getMinOrderAmount());
        voucher.setUsageLimit(req.getMaxUses());
        voucher.setPerUserLimit(1);
        voucher.setStartsAt(startOfDay(req.getStartDate()));
        voucher.setEndsAt(endOfDay(req.getEndDate()));
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setAudience(toVoucherAudience(req.getTargetAudience()));
        voucher.setProductScope(VoucherProductScope.SELLER_PRODUCTS);
        voucher.setStackable(false);
        voucher.setDescription(req.getExtraCondition());
        return voucher;
    }

    private PromotionResponse toPromotionResponse(VoucherResponse voucher) {
        Voucher entity = Voucher.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minimumOrderAmount(voucher.getMinimumOrderAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startsAt(voucher.getStartsAt())
                .endsAt(voucher.getEndsAt())
                .audience(voucher.getAudience())
                .productScope(voucher.getProductScope())
                .status(voucher.getStatus())
                .description(voucher.getDescription())
                .createdAt(voucher.getCreatedAt())
                .build();
        return PromotionResponse.fromVoucher(entity);
    }

    private void validateSimpleVoucher(Voucher voucher, BigDecimal cartTotal) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(voucher.getDeleted())) {
            throw new IllegalArgumentException("Voucher đã bị xóa");
        }
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new IllegalArgumentException("Voucher không đang hoạt động");
        }
        if (voucher.getStartsAt() != null && voucher.getStartsAt().isAfter(now)) {
            throw new IllegalArgumentException("Voucher chua d?n th?i gian s? d?ng");
        }
        if (voucher.getEndsAt() != null && voucher.getEndsAt().isBefore(now)) {
            throw new IllegalArgumentException("Voucher đã hết hạn");
        }
        BigDecimal minimum = voucher.getMinimumOrderAmount() == null ? BigDecimal.ZERO : voucher.getMinimumOrderAmount();
        if (cartTotal != null && cartTotal.compareTo(minimum) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + minimum + "d");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Voucher đã hết lượt sử dụng");
        }
    }

    private VoucherDiscountType toVoucherDiscountType(PromotionType type) {
        if (type == PromotionType.AMOUNT) return VoucherDiscountType.FIXED_AMOUNT;
        if (type == PromotionType.FREESHIP) return VoucherDiscountType.FREE_SHIPPING;
        return VoucherDiscountType.PERCENTAGE;
    }

    private VoucherAudience toVoucherAudience(String targetAudience) {
        if ("new".equalsIgnoreCase(targetAudience) || "new_customer".equalsIgnoreCase(targetAudience)) {
            return VoucherAudience.NEW_CUSTOMER;
        }
        if ("vip".equalsIgnoreCase(targetAudience)) {
            return VoucherAudience.VIP;
        }
        return VoucherAudience.ALL;
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date == null ? LocalDateTime.now() : date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date == null ? LocalDateTime.now().plusDays(30) : date.atTime(LocalTime.MAX);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    public record PromotionSummary(long active, long totalUsed, long expiringSoon) {}
}
