package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.CreateClaimRequest;
import com.example.thuedocosplay.dto.request.CreateInsurancePlanRequest;
import com.example.thuedocosplay.dto.request.ResolveClaimRequest;
import com.example.thuedocosplay.dto.response.*;
import com.example.thuedocosplay.entity.*;
import com.example.thuedocosplay.entity.InsuranceClaim.ClaimStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Đặt tại: service/InsuranceService.java
 */
@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsurancePlanRepository planRepository;
    private final InsuranceClaimRepository claimRepository;
    private final RentalOrderRepository orderRepository;
    private final UserRepository userRepository;

    private static final AtomicLong CLAIM_SEQ = new AtomicLong(1000);

    // ─── PLANS ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InsurancePlanResponse> getActivePlans() {
        return planRepository.findByIsActiveTrueOrderByFeeAmountAsc()
                .stream().map(this::toPlanResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InsurancePlanResponse> getAllPlans() {
        return planRepository.findAll(Sort.by("feeAmount").ascending())
                .stream().map(this::toPlanResponse).toList();
    }

    @Transactional
    public InsurancePlanResponse createPlan(CreateInsurancePlanRequest req) {
        InsurancePlan plan = InsurancePlan.builder()
                .name(req.getName())
                .description(req.getDescription())
                .feeAmount(req.getFeeAmount())
                .coverPercent(req.getCoverPercent())
                .maxPayout(req.getMaxPayout())
                .isActive(true)
                .build();
        return toPlanResponse(planRepository.save(plan));
    }

    @Transactional
    public InsurancePlanResponse updatePlan(Long id, CreateInsurancePlanRequest req) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói bảo hiểm"));
        plan.setName(req.getName());
        plan.setDescription(req.getDescription());
        plan.setFeeAmount(req.getFeeAmount());
        plan.setCoverPercent(req.getCoverPercent());
        plan.setMaxPayout(req.getMaxPayout());
        return toPlanResponse(planRepository.save(plan));
    }

    @Transactional
    public InsurancePlanResponse togglePlan(Long id) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói bảo hiểm"));
        plan.setIsActive(!plan.getIsActive());
        return toPlanResponse(planRepository.save(plan));
    }

    // ─── CLAIMS ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<InsuranceClaimResponse> listClaims(ClaimStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        Page<InsuranceClaim> result = status != null
                ? claimRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : claimRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PagedResponse.of(result.map(this::toClaimResponse));
    }

    @Transactional
    public InsuranceClaimResponse createClaim(String reporterEmail, CreateClaimRequest req) {
        RentalOrder order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        InsurancePlan plan = planRepository.findById(req.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói bảo hiểm"));
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String code = "CLM-" + LocalDateTime.now().getYear()
                + "-" + String.format("%05d", CLAIM_SEQ.incrementAndGet());

        InsuranceClaim claim = InsuranceClaim.builder()
                .claimCode(code)
                .order(order)
                .plan(plan)
                .reporter(reporter)
                .status(ClaimStatus.PENDING)
                .description(req.getDescription())
                .evidenceUrls(req.getEvidenceUrls())
                .requestedAmount(req.getRequestedAmount())
                .build();

        return toClaimResponse(claimRepository.save(claim));
    }

    @Transactional
    public InsuranceClaimResponse resolveClaim(Long claimId, String adminEmail, ResolveClaimRequest req) {
        InsuranceClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy claim"));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin"));

        if (req.getStatus() == ClaimStatus.APPROVED) {
            if (req.getApprovedAmount() == null || req.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cần nhập số tiền bồi hoàn khi duyệt claim");
            }
            // Kiểm tra không vượt maxPayout của gói
            BigDecimal maxPayout = claim.getPlan().getMaxPayout();
            if (maxPayout != null && req.getApprovedAmount().compareTo(maxPayout) > 0) {
                throw new IllegalArgumentException(
                        "Số tiền bồi hoàn vượt giới hạn gói: " + maxPayout);
            }
            claim.setApprovedAmount(req.getApprovedAmount());
        }

        if (req.getStatus() == ClaimStatus.REJECTED && (req.getResolutionNote() == null || req.getResolutionNote().isBlank())) {
            throw new IllegalArgumentException("Cần ghi lý do khi từ chối claim");
        }

        claim.setStatus(req.getStatus());
        claim.setResolutionNote(req.getResolutionNote());
        claim.setResolvedBy(admin);
        claim.setResolvedAt(LocalDateTime.now());

        return toClaimResponse(claimRepository.save(claim));
    }

    // Chuyển sang VERIFYING (đang xác minh)
    @Transactional
    public InsuranceClaimResponse startVerifying(Long claimId) {
        InsuranceClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy claim"));
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể bắt đầu xác minh claim ở trạng thái PENDING");
        }
        claim.setStatus(ClaimStatus.VERIFYING);
        return toClaimResponse(claimRepository.save(claim));
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InsuranceStatsResponse getStats() {
        YearMonth ym = YearMonth.now();
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to   = ym.plusMonths(1).atDay(1).atStartOfDay();

        return InsuranceStatsResponse.builder()
                .pendingClaims(claimRepository.countByStatus(ClaimStatus.PENDING))
                .verifyingClaims(claimRepository.countByStatus(ClaimStatus.VERIFYING))
                .approvedClaims(claimRepository.countByStatus(ClaimStatus.APPROVED))
                .rejectedClaims(claimRepository.countByStatus(ClaimStatus.REJECTED))
                .totalClaims(claimRepository.count())
                .totalPaidOut(claimRepository.sumApprovedPayout(from, to))
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private InsurancePlanResponse toPlanResponse(InsurancePlan p) {
        return InsurancePlanResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .feeAmount(p.getFeeAmount())
                .coverPercent(p.getCoverPercent())
                .maxPayout(p.getMaxPayout())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private InsuranceClaimResponse toClaimResponse(InsuranceClaim c) {
        return InsuranceClaimResponse.builder()
                .id(c.getId())
                .claimCode(c.getClaimCode())
                .orderCode(c.getOrder().getOrderCode())
                .customerName(c.getOrder().getCustomerName())
                .customerPhone(c.getOrder().getCustomerPhone())
                .planName(c.getPlan().getName())
                .planFee(c.getPlan().getFeeAmount())
                .status(c.getStatus())
                .description(c.getDescription())
                .evidenceUrls(c.getEvidenceUrls())
                .requestedAmount(c.getRequestedAmount())
                .approvedAmount(c.getApprovedAmount())
                .resolutionNote(c.getResolutionNote())
                .resolvedByName(c.getResolvedBy() != null ? c.getResolvedBy().getFullName() : null)
                .resolvedAt(c.getResolvedAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
