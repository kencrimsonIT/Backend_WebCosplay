package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.UpsertPromotionRequest;
import com.example.thuedocosplay.dto.response.PromotionResponse;
import com.example.thuedocosplay.entity.Promotion;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.PromotionStatus;
import com.example.thuedocosplay.entity.enums.UserRole;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.PromotionRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> listBySeller(String email) {
        User seller = requireSeller(email);
        return promotionRepository.findBySeller_IdOrderByCreatedAtDesc(seller.getId())
                .stream().map(PromotionResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public PromotionResponse create(String email, UpsertPromotionRequest req) {
        User seller = requireSeller(email);

        String upperCode = req.getCode().toUpperCase();
        if (promotionRepository.existsByCodeIgnoreCase(upperCode)) {
            throw new IllegalArgumentException("Mã voucher '" + upperCode + "' đã tồn tại");
        }

        Promotion promotion = Promotion.builder()
                .seller(seller)
                .code(upperCode)
                .title(req.getTitle())
                .type(req.getType())
                .value(req.getValue())
                .minOrderAmount(req.getMinOrderAmount())
                .maxUses(req.getMaxUses())
                .usedCount(0)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .targetAudience(req.getTargetAudience() != null ? req.getTargetAudience() : "all")
                .applyTo(req.getApplyTo() != null ? req.getApplyTo() : "all-costumes")
                .extraCondition(req.getExtraCondition())
                .status(PromotionStatus.ACTIVE)
                .build();

        return PromotionResponse.from(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse update(String email, Long id, UpsertPromotionRequest req) {
        User seller = requireSeller(email);
        Promotion promotion = getOwnedPromotion(seller, id);

        String upperCode = req.getCode().toUpperCase();
        if (!promotion.getCode().equalsIgnoreCase(upperCode)
                && promotionRepository.existsByCodeIgnoreCase(upperCode)) {
            throw new IllegalArgumentException("Mã voucher '" + upperCode + "' đã tồn tại");
        }

        promotion.setCode(upperCode);
        promotion.setTitle(req.getTitle());
        promotion.setType(req.getType());
        promotion.setValue(req.getValue());
        promotion.setMinOrderAmount(req.getMinOrderAmount());
        promotion.setMaxUses(req.getMaxUses());
        promotion.setStartDate(req.getStartDate());
        promotion.setEndDate(req.getEndDate());
        promotion.setTargetAudience(req.getTargetAudience());
        promotion.setApplyTo(req.getApplyTo());
        promotion.setExtraCondition(req.getExtraCondition());

        return PromotionResponse.from(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse toggleStatus(String email, Long id) {
        User seller = requireSeller(email);
        Promotion promotion = getOwnedPromotion(seller, id);

        if (promotion.getStatus() == PromotionStatus.ACTIVE) {
            promotion.setStatus(PromotionStatus.PAUSED);
        } else {
            promotion.setStatus(PromotionStatus.ACTIVE);
        }

        return PromotionResponse.from(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse duplicate(String email, Long id) {
        User seller = requireSeller(email);
        Promotion source = getOwnedPromotion(seller, id);

        // Generate unique code
        String newCode = source.getCode() + "_COPY";
        int attempt = 0;
        while (promotionRepository.existsByCodeIgnoreCase(newCode)) {
            attempt++;
            newCode = source.getCode() + "_COPY" + attempt;
        }

        Promotion copy = Promotion.builder()
                .seller(seller)
                .code(newCode)
                .title(source.getTitle() + " (bản sao)")
                .type(source.getType())
                .value(source.getValue())
                .minOrderAmount(source.getMinOrderAmount())
                .maxUses(source.getMaxUses())
                .usedCount(0)
                .startDate(source.getStartDate())
                .endDate(source.getEndDate())
                .targetAudience(source.getTargetAudience())
                .applyTo(source.getApplyTo())
                .extraCondition(source.getExtraCondition())
                .status(PromotionStatus.DRAFT)
                .build();

        return PromotionResponse.from(promotionRepository.save(copy));
    }

    @Transactional
    public void delete(String email, Long id) {
        User seller = requireSeller(email);
        Promotion promotion = getOwnedPromotion(seller, id);
        promotionRepository.delete(promotion);
    }

    @Transactional(readOnly = true)
    public PromotionSummary getSummary(String email) {
        User seller = requireSeller(email);
        long active = promotionRepository.countActiveBySeller(seller.getId());
        Long totalUsed = promotionRepository.sumUsedCountBySeller(seller.getId());
        long expiringSoon = promotionRepository
                .findActiveBySellerAndDate(seller.getId(), LocalDate.now())
                .stream()
                .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(LocalDate.now().plusDays(4)))
                .count();

        return new PromotionSummary(active, totalUsed != null ? totalUsed : 0L, expiringSoon);
    }

    @Transactional
    public PromotionResponse checkAndApplyPromotion(String code, BigDecimal cartTotal) {
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không tồn tại"));

        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn hoặc tạm dừng");
        }

        if (promotion.getStartDate() != null && promotion.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Mã khuyến mãi chưa tới thời gian áp dụng");
        }

        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn");
        }

        if (promotion.getMaxUses() != null && promotion.getUsedCount() >= promotion.getMaxUses()) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        if (promotion.getMinOrderAmount() != null && cartTotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + promotion.getMinOrderAmount() + "đ");
        }

        return PromotionResponse.from(promotion);
    }

    @Transactional
    public void incrementUsedCount(String code) {
        if (code == null || code.isBlank()) return;
        promotionRepository.findByCodeIgnoreCase(code).ifPresent(p -> {
            p.setUsedCount(p.getUsedCount() + 1);
            promotionRepository.save(p);
        });
    }

    // ---- helpers ----

    private User requireSeller(String email) {
        if (email == null) throw new AuthenticationCredentialsNotFoundException("Chưa đăng nhập");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (user.getRole() != UserRole.SELLER) throw new AccessDeniedException("Chỉ seller mới có quyền này");
        return user;
    }

    private Promotion getOwnedPromotion(User seller, Long id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi #" + id));
        if (!p.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập khuyến mãi này");
        }
        return p;
    }

    public record PromotionSummary(long active, long totalUsed, long expiringSoon) {}
}
