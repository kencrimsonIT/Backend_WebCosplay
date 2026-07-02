package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.ApplyVoucherRequest;
import com.example.thuedocosplay.dto.request.CreateOrderItemRequest;
import com.example.thuedocosplay.dto.request.CreateOrderRequest;
import com.example.thuedocosplay.dto.request.UpsertVoucherRequest;
import com.example.thuedocosplay.dto.response.VoucherApplyResponse;
import com.example.thuedocosplay.dto.response.VoucherResponse;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.Voucher;
import com.example.thuedocosplay.entity.VoucherUsage;
import com.example.thuedocosplay.entity.enums.UserRole;
import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import com.example.thuedocosplay.entity.enums.VoucherProductScope;
import com.example.thuedocosplay.entity.enums.VoucherStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.UserRepository;
import com.example.thuedocosplay.repository.VoucherRepository;
import com.example.thuedocosplay.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository usageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<VoucherResponse> listSellerVouchers(String currentUserEmail) {
        User seller = requireSeller(currentUserEmail);
        return voucherRepository.findVisibleBySellerId(seller.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VoucherResponse createSellerVoucher(String currentUserEmail, UpsertVoucherRequest request) {
        User seller = requireSeller(currentUserEmail);
        String code = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Mã voucher da ton tai");
        }
        normalizeVoucherDefaults(request);
        validateVoucherRequest(request);

        Voucher voucher = Voucher.builder()
                .seller(seller)
                .code(code)
                .title(request.getTitle().trim())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minimumOrderAmount(nullToZero(request.getMinimumOrderAmount()))
                .usageLimit(request.getUsageLimit())
                .perUserLimit(request.getPerUserLimit() == null ? 1 : request.getPerUserLimit())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .status(request.getStatus() == null ? VoucherStatus.ACTIVE : request.getStatus())
                .audience(request.getAudience())
                .productScope(VoucherProductScope.SELLER_PRODUCTS)
                .stackable(Boolean.TRUE.equals(request.getStackable()))
                .description(request.getDescription())
                .deleted(false)
                .build();

        Voucher saved = voucherRepository.save(voucher);
        log.info("[Voucher] seller={} created code={}", seller.getEmail(), saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public VoucherResponse updateSellerVoucher(String currentUserEmail, Long voucherId, UpsertVoucherRequest request) {
        User seller = requireSeller(currentUserEmail);
        Voucher voucher = sellerVoucher(voucherId, seller.getId());
        String code = normalizeCode(request.getCode());
        if (!voucher.getCode().equalsIgnoreCase(code) && voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Mã voucher da ton tai");
        }
        normalizeVoucherDefaults(request);
        validateVoucherRequest(request);

        voucher.setCode(code);
        voucher.setTitle(request.getTitle().trim());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinimumOrderAmount(nullToZero(request.getMinimumOrderAmount()));
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setPerUserLimit(request.getPerUserLimit() == null ? 1 : request.getPerUserLimit());
        voucher.setStartsAt(request.getStartsAt());
        voucher.setEndsAt(request.getEndsAt());
        voucher.setStatus(request.getStatus() == null ? voucher.getStatus() : request.getStatus());
        voucher.setAudience(request.getAudience());
        voucher.setProductScope(VoucherProductScope.SELLER_PRODUCTS);
        voucher.setStackable(Boolean.TRUE.equals(request.getStackable()));
        voucher.setDescription(request.getDescription());

        Voucher saved = voucherRepository.save(voucher);
        log.info("[Voucher] seller={} updated code={}", seller.getEmail(), saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public VoucherResponse duplicateSellerVoucher(String currentUserEmail, Long voucherId) {
        User seller = requireSeller(currentUserEmail);
        Voucher source = sellerVoucher(voucherId, seller.getId());
        String newCode = uniqueCopyCode(source.getCode());

        Voucher copy = Voucher.builder()
                .seller(seller)
                .code(newCode)
                .title(source.getTitle() + " (copy)")
                .discountType(source.getDiscountType())
                .discountValue(source.getDiscountValue())
                .maxDiscountAmount(source.getMaxDiscountAmount())
                .minimumOrderAmount(nullToZero(source.getMinimumOrderAmount()))
                .usageLimit(source.getUsageLimit())
                .perUserLimit(source.getPerUserLimit())
                .usedCount(0)
                .startsAt(source.getStartsAt())
                .endsAt(source.getEndsAt())
                .status(VoucherStatus.DRAFT)
                .audience(source.getAudience())
                .productScope(source.getProductScope())
                .stackable(Boolean.TRUE.equals(source.getStackable()))
                .description(source.getDescription())
                .deleted(false)
                .build();

        Voucher saved = voucherRepository.save(copy);
        log.info("[Voucher] seller={} duplicated source={} copy={}", seller.getEmail(), source.getCode(), saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public void deleteSellerVoucher(String currentUserEmail, Long voucherId) {
        User seller = requireSeller(currentUserEmail);
        Voucher voucher = sellerVoucher(voucherId, seller.getId());
        voucher.setDeleted(true);
        voucher.setStatus(VoucherStatus.EXPIRED);
        voucher.setCode(archivedCode(voucher));
        voucherRepository.save(voucher);
        log.info("[Voucher] seller={} deleted voucherId={}", seller.getEmail(), voucherId);
    }

    @Transactional(readOnly = true)
    public VoucherSummary getSellerSummary(String currentUserEmail) {
        User seller = requireSeller(currentUserEmail);
        LocalDateTime now = LocalDateTime.now();
        return new VoucherSummary(
                voucherRepository.countActiveBySeller(seller.getId()),
                voucherRepository.sumUsedCountBySeller(seller.getId()),
                voucherRepository.countExpiringSoonBySeller(seller.getId(), now, now.plusDays(3))
        );
    }

    @Transactional
    public VoucherResponse toggleSellerVoucher(String currentUserEmail, Long voucherId) {
        User seller = requireSeller(currentUserEmail);
        Voucher voucher = sellerVoucher(voucherId, seller.getId());
        voucher.setStatus(voucher.getStatus() == VoucherStatus.PAUSED ? VoucherStatus.ACTIVE : VoucherStatus.PAUSED);
        Voucher saved = voucherRepository.save(voucher);
        log.info("[Voucher] seller={} toggled code={} status={}", seller.getEmail(), saved.getCode(), saved.getStatus());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VoucherApplyResponse previewVoucher(String currentUserEmail, ApplyVoucherRequest request) {
        User user = currentUser(currentUserEmail);
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(normalizeCode(request.getCode()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
        return calculateVoucher(voucher, request.getItems(), request.getRentalTotal(), request.getWarrantyTotal(),
                request.getDepositTotal(), user, user != null ? user.getEmail() : null);
    }

    @Transactional
    public VoucherApplyResponse applyVoucherToOrder(CreateOrderRequest request, User customer, RentalOrder order) {
        if (request.getVoucherCode() == null || request.getVoucherCode().isBlank()) {
            order.setDiscountTotal(BigDecimal.ZERO);
            return null;
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(normalizeCode(request.getVoucherCode()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
        VoucherApplyResponse result = calculateVoucher(
                voucher,
                request.getItems(),
                request.getRentalTotal(),
                request.getWarrantyTotal(),
                request.getDepositTotal(),
                customer,
                customer != null ? customer.getEmail() : request.getCustomerEmail()
        );

        order.setVoucherCode(voucher.getCode());
        order.setVoucherTitle(voucher.getTitle());
        order.setDiscountTotal(result.getDiscountAmount());
        order.setGrandTotal(order.getRentalTotal()
                .add(order.getWarrantyTotal())
                .add(order.getDepositTotal())
                .subtract(result.getDiscountAmount())
                .max(BigDecimal.ZERO));

        return result;
    }

    @Transactional
    public void recordVoucherUsage(VoucherApplyResponse result, CreateOrderRequest request, User customer, RentalOrder order) {
        if (result == null || result.getVoucherId() == null) {
            return;
        }
        Voucher voucher = voucherRepository.findById(result.getVoucherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
        usageRepository.save(VoucherUsage.builder()
                .voucher(voucher)
                .order(order)
                .customer(customer)
                .customerEmail(customer != null ? customer.getEmail() : request.getCustomerEmail())
                .discountAmount(result.getDiscountAmount())
                .counted(false)
                .build());
        log.info("[Voucher] reserved code={} order={} discount={}", voucher.getCode(), order.getOrderCode(), result.getDiscountAmount());
    }

    @Transactional
    public void confirmVoucherUsage(RentalOrder order) {
        if (order == null || order.getId() == null) {
            return;
        }
        usageRepository.findByOrder_Id(order.getId()).ifPresent(usage -> {
            if (Boolean.TRUE.equals(usage.getCounted())) {
                return;
            }
            Voucher voucher = usage.getVoucher();
            voucher.setUsedCount((voucher.getUsedCount() == null ? 0 : voucher.getUsedCount()) + 1);
            usage.setCounted(true);
            voucherRepository.save(voucher);
            usageRepository.save(usage);
            log.info("[Voucher] confirmed code={} order={} usedCount={}", voucher.getCode(), order.getOrderCode(), voucher.getUsedCount());
        });
    }

    @Transactional
    public void releasePendingVoucherUsage(RentalOrder order) {
        if (order == null || order.getId() == null) {
            return;
        }
        usageRepository.findByOrder_Id(order.getId()).ifPresent(usage -> {
            if (Boolean.TRUE.equals(usage.getCounted())) {
                return;
            }
            String code = usage.getVoucher() != null ? usage.getVoucher().getCode() : order.getVoucherCode();
            usageRepository.delete(usage);
            order.setVoucherCode(null);
            order.setVoucherTitle(null);
            order.setDiscountTotal(BigDecimal.ZERO);
            order.setGrandTotal(order.getRentalTotal()
                    .add(order.getWarrantyTotal())
                    .add(order.getDepositTotal())
                    .max(BigDecimal.ZERO));
            log.info("[Voucher] released pending code={} order={}", code, order.getOrderCode());
        });
    }

    private VoucherApplyResponse calculateVoucher(
            Voucher voucher,
            List<CreateOrderItemRequest> items,
            BigDecimal rentalTotal,
            BigDecimal warrantyTotal,
            BigDecimal depositTotal,
            User user,
            String customerEmail
    ) {
        validateUsable(voucher, customerEmail);
        BigDecimal eligibleSubtotal = eligibleSubtotal(voucher, items);
        BigDecimal minimum = nullToZero(voucher.getMinimumOrderAmount());
        if (eligibleSubtotal.compareTo(minimum) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu của voucher");
        }

        BigDecimal discount = calculateDiscount(voucher, eligibleSubtotal);
        BigDecimal payable = nullToZero(rentalTotal)
                .add(nullToZero(warrantyTotal))
                .add(nullToZero(depositTotal))
                .subtract(discount)
                .max(BigDecimal.ZERO);
        long usedCount = usageRepository.countByVoucher_Id(voucher.getId());
        long userUsedCount = customerEmail == null
                ? 0
                : usageRepository.countByVoucher_IdAndCustomerEmailIgnoreCase(voucher.getId(), customerEmail);

        return VoucherApplyResponse.builder()
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .eligibleSubtotal(eligibleSubtotal)
                .discountAmount(discount)
                .payableTotal(payable)
                .stackable(voucher.getStackable())
                .usageLimit(voucher.getUsageLimit())
                .perUserLimit(voucher.getPerUserLimit())
                .usedCount(usedCount)
                .userUsedCount(userUsedCount)
                .message("Áp dụng voucher thành công")
                .build();
    }

    private void validateUsable(Voucher voucher, String customerEmail) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(voucher.getDeleted())) {
            throw new IllegalArgumentException("Voucher đã bị xóa");
        }
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new IllegalArgumentException("Voucher không đang hoạt động");
        }
        if (voucher.getStartsAt() != null && now.isBefore(voucher.getStartsAt())) {
            throw new IllegalArgumentException("Voucher chua d?n th?i gian s? d?ng");
        }
        if (voucher.getEndsAt() != null && now.isAfter(voucher.getEndsAt())) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            throw new IllegalArgumentException("Voucher đã hết hạn");
        }
        if (voucher.getUsageLimit() != null && usageRepository.countByVoucher_Id(voucher.getId()) >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Voucher đã hết lượt sử dụng");
        }
        if (customerEmail != null && voucher.getPerUserLimit() != null && voucher.getPerUserLimit() > 0
                && usageRepository.countByVoucher_IdAndCustomerEmailIgnoreCase(voucher.getId(), customerEmail) >= voucher.getPerUserLimit()) {
            throw new IllegalArgumentException("Bạn đã dùng voucher này hết số lượt cho phép");
        }
    }

    private BigDecimal eligibleSubtotal(Voucher voucher, List<CreateOrderItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequest item : items) {
            if (item.getProductId() == null || item.getLineTotal() == null) continue;
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null) continue;
            boolean eligible = voucher.getProductScope() == VoucherProductScope.ALL_PRODUCTS
                    || (product.getSeller() != null && voucher.getSeller() != null
                    && product.getSeller().getId().equals(voucher.getSeller().getId()))
                    || (product.getSeller() == null && voucher.getSeller() != null);
            if (eligible) {
                subtotal = subtotal.add(item.getLineTotal());
            }
        }
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Voucher không áp dụng cho sản phẩm trong giỏ hàng");
        }
        return subtotal;
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal eligibleSubtotal) {
        BigDecimal discount;
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            discount = eligibleSubtotal
                    .multiply(voucher.getDiscountValue())
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        } else if (voucher.getDiscountType() == VoucherDiscountType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        } else {
            discount = BigDecimal.ZERO;
        }
        return discount.min(eligibleSubtotal).max(BigDecimal.ZERO);
    }

    private void validateVoucherRequest(UpsertVoucherRequest request) {
        if (request.getStartsAt().isAfter(request.getEndsAt())) {
            throw new IllegalArgumentException("Ngày bắt đầu khong duoc sau ngay ket thuc");
        }
        if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0 && request.getDiscountType() != VoucherDiscountType.FREE_SHIPPING) {
            throw new IllegalArgumentException("Giá trị giảm phai lon hon 0");
        }
        if (request.getDiscountType() == VoucherDiscountType.PERCENTAGE
                && request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Voucher phần trăm không được vượt quá 100%");
        }
    }

    private void normalizeVoucherDefaults(UpsertVoucherRequest request) {
        if (request.getStartsAt() == null) {
            request.setStartsAt(LocalDateTime.now());
        }
        if (request.getEndsAt() == null) {
            request.setEndsAt(LocalDateTime.now().plusDays(30));
        }
        if (request.getAudience() == null) {
            request.setAudience(com.example.thuedocosplay.entity.enums.VoucherAudience.ALL);
        }
        if (request.getProductScope() == null) {
            request.setProductScope(VoucherProductScope.SELLER_PRODUCTS);
        }
        if (request.getMinimumOrderAmount() == null) {
            request.setMinimumOrderAmount(BigDecimal.ZERO);
        }
        if (request.getPerUserLimit() == null) {
            request.setPerUserLimit(1);
        }
    }

    private Voucher sellerVoucher(Long voucherId, Long sellerId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
        if (voucher.getSeller() == null || !voucher.getSeller().getId().equals(sellerId)) {
            throw new AccessDeniedException("Bạn không có quyền quản lý voucher này");
        }
        return voucher;
    }

    private User requireSeller(String email) {
        User user = currentUser(email);
        if (user == null || (user.getRole() != UserRole.SELLER && user.getRole() != UserRole.ADMIN)) {
            throw new AccessDeniedException("Chỉ seller hoặc admin được quản lý voucher");
        }
        return user;
    }

    private User currentUser(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private String uniqueCopyCode(String originalCode) {
        String base = normalizeCode(originalCode);
        String stem = base.length() > 31 ? base.substring(0, 31) : base;
        String candidate = stem + "_COPY";
        int attempt = 1;
        while (voucherRepository.existsByCodeIgnoreCase(candidate)) {
            String suffix = "_COPY" + attempt++;
            int maxStem = Math.max(1, 40 - suffix.length());
            candidate = (base.length() > maxStem ? base.substring(0, maxStem) : base) + suffix;
        }
        return candidate;
    }

    private String archivedCode(Voucher voucher) {
        String suffix = "_DEL_" + voucher.getId();
        String base = normalizeCode(voucher.getCode());
        int maxBase = Math.max(1, 40 - suffix.length());
        String candidate = (base.length() > maxBase ? base.substring(0, maxBase) : base) + suffix;
        int attempt = 1;
        while (voucherRepository.existsByCodeIgnoreCase(candidate)) {
            String retrySuffix = suffix + "_" + attempt++;
            maxBase = Math.max(1, 40 - retrySuffix.length());
            candidate = (base.length() > maxBase ? base.substring(0, maxBase) : base) + retrySuffix;
        }
        return candidate;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public VoucherResponse toResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .sellerId(voucher.getSeller() != null ? voucher.getSeller().getId() : null)
                .sellerName(voucher.getSeller() != null ? voucher.getSeller().getFullName() : null)
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minimumOrderAmount(voucher.getMinimumOrderAmount())
                .usageLimit(voucher.getUsageLimit())
                .perUserLimit(voucher.getPerUserLimit())
                .usedCount(voucher.getUsedCount())
                .startsAt(voucher.getStartsAt())
                .endsAt(voucher.getEndsAt())
                .status(voucher.getStatus())
                .audience(voucher.getAudience())
                .productScope(voucher.getProductScope())
                .stackable(voucher.getStackable())
                .description(voucher.getDescription())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }

    public record VoucherSummary(long active, long totalUsed, long expiringSoon) {}
}
