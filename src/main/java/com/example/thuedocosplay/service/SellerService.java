package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.request.UpsertProductRequest;
import com.example.thuedocosplay.dto.response.SellerDashboardResponse;
import com.example.thuedocosplay.dto.response.SellerOrderResponse;
import com.example.thuedocosplay.dto.response.SellerProductResponse;
import com.example.thuedocosplay.dto.response.SellerRevenueResponse;
import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.entity.OrderItem;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import com.example.thuedocosplay.entity.enums.UserRole;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM,
            OrderStatus.CONFIRMED,
            OrderStatus.RENTING,
            OrderStatus.COMPLETED
    );

    private static final List<OrderStatus> ACTIVE_DEPOSIT_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM,
            OrderStatus.CONFIRMED,
            OrderStatus.RENTING
    );

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RentalOrderRepository orderRepository;
    private final CategoryService categoryService;
    private final VoucherService voucherService;

    @Transactional(readOnly = true)
    public SellerDashboardResponse dashboard(String currentUserEmail) {
        User seller = requireSeller(currentUserEmail);
        List<RentalOrder> orders = orderRepository.findSellerOrders(seller.getId(), null, null, null);
        SellerMoneySummary money = new SellerMoneySummary();
        long pending = 0;
        long confirmed = 0;
        long renting = 0;
        long completed = 0;
        long cancelled = 0;
        long returnDue = 0;
        LocalDate today = LocalDate.now();

        for (RentalOrder order : orders) {
            money.addOrder(order, seller.getId());
            if (order.getStatus() == OrderStatus.PENDING_CONFIRM || order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                pending++;
            } else if (order.getStatus() == OrderStatus.CONFIRMED) {
                confirmed++;
            } else if (order.getStatus() == OrderStatus.RENTING) {
                renting++;
            } else if (order.getStatus() == OrderStatus.COMPLETED) {
                completed++;
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                cancelled++;
            }
            if (order.getStatus() == OrderStatus.RENTING && order.getRentTo() != null && !order.getRentTo().isAfter(today.plusDays(2))) {
                returnDue++;
            }
        }

        List<SellerProductResponse> lowStockProducts = productRepository.findAllBySeller_IdOrderByIdDesc(seller.getId()).stream()
                .filter(product -> product.getQuantity() != null && product.getQuantity() <= 1)
                .limit(6)
                .map(this::toProductResponse)
                .toList();

        List<SellerOrderResponse> recentOrders = orders.stream()
                .limit(6)
                .map(order -> toOrderResponse(order, seller.getId()))
                .toList();

        log.info("[Seller] Dashboard sellerId={} email={} productCount={} orderCount={} rentalRevenue={} depositHeld={}",
                seller.getId(), seller.getEmail(), productRepository.countBySeller_Id(seller.getId()), orders.size(), money.rentalRevenue, money.depositHeld);

        return SellerDashboardResponse.builder()
                .sellerId(seller.getId())
                .sellerName(seller.getFullName())
                .sellerEmail(seller.getEmail())
                .productCount(productRepository.countBySeller_Id(seller.getId()))
                .visibleProductCount(productRepository.countBySeller_IdAndVisible(seller.getId(), true))
                .hiddenProductCount(productRepository.countBySeller_IdAndVisible(seller.getId(), false))
                .availableProductCount(productRepository.countBySeller_IdAndInventoryStatus(seller.getId(), ProductInventoryStatus.AVAILABLE))
                .maintenanceProductCount(productRepository.countBySeller_IdAndInventoryStatus(seller.getId(), ProductInventoryStatus.MAINTENANCE))
                .soldProductCount(productRepository.countBySeller_IdAndInventoryStatus(seller.getId(), ProductInventoryStatus.SOLD))
                .totalOrderCount(orders.size())
                .pendingOrderCount(pending)
                .confirmedOrderCount(confirmed)
                .rentingOrderCount(renting)
                .completedOrderCount(completed)
                .cancelledOrderCount(cancelled)
                .returnDueOrderCount(returnDue)
                .rentalRevenue(money.rentalRevenue)
                .depositCollected(money.depositCollected)
                .depositHeld(money.depositHeld)
                .estimatedReceivable(money.estimatedReceivable())
                .averageOrderValue(money.averageOrderValue())
                .recentOrders(recentOrders)
                .lowStockProducts(lowStockProducts)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SellerProductResponse> listProducts(String currentUserEmail) {
        User seller = requireSeller(currentUserEmail);
        List<SellerProductResponse> products = productRepository.findAllBySeller_IdOrderByIdDesc(seller.getId()).stream()
                .map(this::toProductResponse)
                .toList();
        log.info("[SellerProduct] Listed sellerId={} email={} productCount={}", seller.getId(), seller.getEmail(), products.size());
        return products;
    }

    @Transactional
    public SellerProductResponse createProduct(String currentUserEmail, UpsertProductRequest request) {
        User seller = requireSeller(currentUserEmail);
        Product product = Product.builder()
                .name(request.getName())
                .category(findCategory(request.getCategoryId()))
                .seller(seller)
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .deposit(request.getDeposit())
                .imageUrl(request.getImageUrl())
                .visible(request.getVisible() != null ? request.getVisible() : true)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .inventoryStatus(request.getInventoryStatus() != null ? request.getInventoryStatus() : ProductInventoryStatus.AVAILABLE)
                .build();
        Product saved = productRepository.save(product);
        log.info("[SellerProduct] Created sellerId={} productId={} name={} pricePerDay={} deposit={} quantity={} visible={} inventoryStatus={}",
                seller.getId(), saved.getId(), saved.getName(), saved.getPricePerDay(), saved.getDeposit(), saved.getQuantity(), saved.getVisible(), saved.getInventoryStatus());
        return toProductResponse(saved);
    }

    @Transactional
    public SellerProductResponse updateProduct(String currentUserEmail, Long productId, UpsertProductRequest request) {
        User seller = requireSeller(currentUserEmail);
        Product product = findSellerProduct(productId, seller.getId());
        product.setName(request.getName());
        product.setCategory(findCategory(request.getCategoryId()));
        product.setDescription(request.getDescription());
        product.setPricePerDay(request.getPricePerDay());
        product.setDeposit(request.getDeposit());
        product.setImageUrl(request.getImageUrl());
        if (request.getVisible() != null) {
            product.setVisible(request.getVisible());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getInventoryStatus() != null) {
            product.setInventoryStatus(request.getInventoryStatus());
        }
        Product saved = productRepository.save(product);
        log.info("[SellerProduct] Updated sellerId={} productId={} name={} pricePerDay={} deposit={} quantity={} visible={} inventoryStatus={}",
                seller.getId(), saved.getId(), saved.getName(), saved.getPricePerDay(), saved.getDeposit(), saved.getQuantity(), saved.getVisible(), saved.getInventoryStatus());
        return toProductResponse(saved);
    }

    @Transactional
    public SellerProductResponse toggleProduct(String currentUserEmail, Long productId) {
        User seller = requireSeller(currentUserEmail);
        Product product = findSellerProduct(productId, seller.getId());
        product.setVisible(!Boolean.TRUE.equals(product.getVisible()));
        Product saved = productRepository.save(product);
        log.info("[SellerProduct] Toggled visibility sellerId={} productId={} visible={}", seller.getId(), saved.getId(), saved.getVisible());
        return toProductResponse(saved);
    }

    @Transactional
    public void deleteProduct(String currentUserEmail, Long productId) {
        User seller = requireSeller(currentUserEmail);
        Product product = findSellerProduct(productId, seller.getId());
        long orderItemCount = productRepository.countOrderItemsByProductId(productId);
        if (orderItemCount > 0) {
            product.setVisible(false);
            product.setInventoryStatus(ProductInventoryStatus.SOLD);
            productRepository.save(product);
            log.info("[SellerProduct] Soft-deleted sellerId={} productId={} name={} orderItemCount={}",
                    seller.getId(), productId, product.getName(), orderItemCount);
            return;
        }
        productRepository.delete(product);
        log.info("[SellerProduct] Deleted sellerId={} productId={} name={} orderItemCount=0", seller.getId(), productId, product.getName());
    }

    @Transactional(readOnly = true)
    public List<SellerOrderResponse> listOrders(String currentUserEmail, OrderStatus status, LocalDate fromDate, LocalDate toDate) {
        User seller = requireSeller(currentUserEmail);
        validateDateRange(fromDate, toDate);
        List<SellerOrderResponse> orders = orderRepository.findSellerOrders(seller.getId(), status, fromDate, toDate).stream()
                .map(order -> toOrderResponse(order, seller.getId()))
                .toList();
        log.info("[SellerOrder] Listed sellerId={} status={} fromDate={} toDate={} orderCount={}",
                seller.getId(), status, fromDate, toDate, orders.size());
        return orders;
    }

    @Transactional(readOnly = true)
    public SellerOrderResponse getOrder(String currentUserEmail, Long orderId) {
        User seller = requireSeller(currentUserEmail);
        RentalOrder order = orderRepository.findSellerOrderDetail(orderId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don hang cua nguoi ban"));
        log.info("[SellerOrder] Viewed detail sellerId={} orderId={} orderCode={} status={}",
                seller.getId(), order.getId(), order.getOrderCode(), order.getStatus());
        return toOrderResponse(order, seller.getId());
    }

    @Transactional
    public SellerOrderResponse updateOrderStatus(String currentUserEmail, Long orderId, UpdateOrderStatusRequest request) {
        User seller = requireSeller(currentUserEmail);
        RentalOrder order = orderRepository.findSellerOrderDetail(orderId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don hang cua nguoi ban"));
        validateSellerStatusUpdate(request.getStatus());
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED && order.getPaidAt() == null) {
            order.setPaidAt(LocalDateTime.now());
        }
        syncVoucherUsageForStatus(order, oldStatus, request.getStatus());
        RentalOrder saved = orderRepository.save(order);
        log.info("[SellerOrder] Updated status sellerId={} orderId={} orderCode={} oldStatus={} newStatus={}",
                seller.getId(), saved.getId(), saved.getOrderCode(), oldStatus, saved.getStatus());
        return toOrderResponse(saved, seller.getId());
    }

    @Transactional(readOnly = true)
    public SellerRevenueResponse revenue(String currentUserEmail, LocalDate fromDate, LocalDate toDate, String groupBy) {
        User seller = requireSeller(currentUserEmail);
        LocalDate now = LocalDate.now();
        LocalDate resolvedTo = toDate != null ? toDate : now;
        LocalDate resolvedFrom = fromDate != null ? fromDate : resolvedTo.minusDays(29);
        RevenueGroup group = RevenueGroup.from(groupBy);
        validateDateRange(resolvedFrom, resolvedTo);

        List<RentalOrder> orders = orderRepository.findSellerRevenueOrders(
                seller.getId(),
                resolvedFrom.atStartOfDay(),
                resolvedTo.plusDays(1).atStartOfDay(),
                REVENUE_STATUSES
        );

        Map<String, RevenueBucket> buckets = createRevenueBuckets(resolvedFrom, resolvedTo, group);
        SellerMoneySummary summary = new SellerMoneySummary();

        for (RentalOrder order : orders) {
            SellerMoneySummary orderMoney = sellerMoney(order, seller.getId());
            summary.add(order, orderMoney);
            RevenueBucket bucket = buckets.get(group.key(order.getPaidAt().toLocalDate()));
            if (bucket != null) {
                bucket.add(order, orderMoney);
            }
        }

        List<SellerRevenueResponse.Bar> bars = buckets.values().stream()
                .map(RevenueBucket::toBar)
                .toList();

        log.info("[SellerRevenue] sellerId={} fromDate={} toDate={} groupBy={} orderCount={} rentalRevenue={} depositHeld={}",
                seller.getId(), resolvedFrom, resolvedTo, group.value, summary.orderCount, summary.rentalRevenue, summary.depositHeld);

        return SellerRevenueResponse.builder()
                .fromDate(resolvedFrom)
                .toDate(resolvedTo)
                .groupBy(group.value)
                .summary(SellerRevenueResponse.Summary.builder()
                        .rentalRevenue(summary.rentalRevenue)
                        .depositCollected(summary.depositCollected)
                        .depositHeld(summary.depositHeld)
                        .estimatedReceivable(summary.estimatedReceivable())
                        .averageOrderValue(summary.averageOrderValue())
                        .orderCount(summary.orderCount)
                        .completedOrderCount(summary.completedOrderCount)
                        .build())
                .bars(bars)
                .build();
    }

    private User requireSeller(String currentUserEmail) {
        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Vui long dang nhap voi tai khoan nguoi ban");
        }
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tai khoan dang dang nhap"));
        if (user.getRole() != UserRole.SELLER) {
            throw new AccessDeniedException("Chi nguoi ban moi duoc truy cap chuc nang nay");
        }
        return user;
    }

    private Category findCategory(Long id) {
        return categoryService.findById(id);
    }

    private Product findSellerProduct(Long productId, Long sellerId) {
        return productRepository.findByIdAndSeller_Id(productId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay san pham cua nguoi ban"));
    }

    private void validateSellerStatusUpdate(OrderStatus status) {
        if (status == OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Nguoi ban khong duoc chuyen don ve trang thai cho thanh toan");
        }
    }

    private void syncVoucherUsageForStatus(RentalOrder order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.RENTING || newStatus == OrderStatus.COMPLETED) {
            voucherService.confirmVoucherUsage(order);
        } else if (newStatus == OrderStatus.CANCELLED
                && (oldStatus == OrderStatus.PENDING_PAYMENT || oldStatus == OrderStatus.PENDING_CONFIRM)) {
            voucherService.releasePendingVoucherUsage(order);
        }
    }

    private SellerProductResponse toProductResponse(Product product) {
        return SellerProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .description(product.getDescription())
                .pricePerDay(product.getPricePerDay())
                .deposit(product.getDeposit())
                .imageUrl(product.getImageUrl())
                .visible(product.getVisible())
                .quantity(product.getQuantity())
                .inventoryStatus(product.getInventoryStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private SellerOrderResponse toOrderResponse(RentalOrder order, Long sellerId) {
        List<SellerOrderResponse.Item> sellerItems = order.getItems().stream()
                .filter(item -> itemBelongsToSeller(item, sellerId))
                .map(this::toSellerItem)
                .toList();

        SellerMoneySummary money = sellerMoney(order, sellerId);
        return SellerOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .rentFrom(order.getRentFrom())
                .rentTo(order.getRentTo())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .orderRentalTotal(order.getRentalTotal())
                .orderWarrantyTotal(order.getWarrantyTotal())
                .orderDepositTotal(order.getDepositTotal())
                .orderGrandTotal(order.getGrandTotal())
                .sellerRentalTotal(money.rentalRevenue)
                .sellerDepositTotal(money.depositCollected)
                .sellerEstimatedReceivable(money.estimatedReceivable())
                .items(sellerItems)
                .build();
    }

    private SellerOrderResponse.Item toSellerItem(OrderItem item) {
        Product product = item.getProduct();
        BigDecimal depositPerItem = product != null ? safe(product.getDeposit()) : BigDecimal.ZERO;
        BigDecimal depositTotal = depositPerItem.multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0L));
        return SellerOrderResponse.Item.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(item.getProductName())
                .categoryName(item.getCategoryName())
                .productImageUrl(product != null ? product.getImageUrl() : null)
                .size(item.getSize())
                .days(item.getDays())
                .quantity(item.getQuantity())
                .unitPrice(calculateUnitPrice(item))
                .lineTotal(safe(item.getLineTotal()))
                .depositPerItem(depositPerItem)
                .depositTotal(depositTotal)
                .build();
    }

    private SellerMoneySummary sellerMoney(RentalOrder order, Long sellerId) {
        SellerMoneySummary summary = new SellerMoneySummary();
        summary.addOrder(order, sellerId);
        return summary;
    }

    private boolean itemBelongsToSeller(OrderItem item, Long sellerId) {
        return item.getProduct() != null
                && item.getProduct().getSeller() != null
                && sellerId.equals(item.getProduct().getSeller().getId());
    }

    private static BigDecimal calculateUnitPrice(OrderItem item) {
        if (item.getLineTotal() == null || item.getDays() == null || item.getQuantity() == null
                || item.getDays() <= 0 || item.getQuantity() <= 0) {
            return BigDecimal.ZERO;
        }
        return item.getLineTotal()
                .divide(BigDecimal.valueOf((long) item.getDays() * item.getQuantity()), 0, RoundingMode.HALF_UP);
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngay bat dau khong duoc sau ngay ket thuc");
        }
    }

    private Map<String, RevenueBucket> createRevenueBuckets(LocalDate fromDate, LocalDate toDate, RevenueGroup group) {
        Map<String, RevenueBucket> buckets = new LinkedHashMap<>();
        LocalDate cursor = group.periodStart(fromDate);
        LocalDate last = group.periodStart(toDate);
        while (!cursor.isAfter(last)) {
            LocalDate periodStart = cursor.isBefore(fromDate) ? fromDate : cursor;
            LocalDate rawPeriodEnd = group.next(cursor).minusDays(1);
            LocalDate periodEnd = rawPeriodEnd.isAfter(toDate) ? toDate : rawPeriodEnd;
            RevenueBucket bucket = new RevenueBucket(group.key(cursor), group.label(cursor), periodStart, periodEnd);
            buckets.put(bucket.key, bucket);
            cursor = group.next(cursor);
        }
        return buckets;
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private enum RevenueGroup {
        DAY("day"),
        MONTH("month"),
        YEAR("year");

        private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ISO_LOCAL_DATE;
        private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM", Locale.US);
        private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US);
        private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy", Locale.US);
        private static final DateTimeFormatter YEAR_KEY = DateTimeFormatter.ofPattern("yyyy", Locale.US);

        private final String value;

        RevenueGroup(String value) {
            this.value = value;
        }

        static RevenueGroup from(String value) {
            if (value == null || value.isBlank()) {
                return DAY;
            }
            for (RevenueGroup group : values()) {
                if (group.value.equalsIgnoreCase(value)) {
                    return group;
                }
            }
            throw new IllegalArgumentException("groupBy chi ho tro: day, month, year");
        }

        String key(LocalDate date) {
            return switch (this) {
                case DAY -> DAY_KEY.format(date);
                case MONTH -> MONTH_KEY.format(date);
                case YEAR -> YEAR_KEY.format(date);
            };
        }

        String label(LocalDate date) {
            return switch (this) {
                case DAY -> DAY_LABEL.format(date);
                case MONTH -> MONTH_LABEL.format(date);
                case YEAR -> YEAR_KEY.format(date);
            };
        }

        LocalDate periodStart(LocalDate date) {
            return switch (this) {
                case DAY -> date;
                case MONTH -> date.withDayOfMonth(1);
                case YEAR -> date.withDayOfYear(1);
            };
        }

        LocalDate next(LocalDate date) {
            return switch (this) {
                case DAY -> date.plusDays(1);
                case MONTH -> date.plusMonths(1).withDayOfMonth(1);
                case YEAR -> date.plusYears(1).withDayOfYear(1);
            };
        }
    }

    private static class SellerMoneySummary {
        protected BigDecimal rentalRevenue = BigDecimal.ZERO;
        protected BigDecimal depositCollected = BigDecimal.ZERO;
        protected BigDecimal depositHeld = BigDecimal.ZERO;
        protected long orderCount;
        protected long completedOrderCount;

        void addOrder(RentalOrder order, Long sellerId) {
            SellerMoneySummary orderMoney = new SellerMoneySummary();
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null
                        && item.getProduct().getSeller() != null
                        && sellerId.equals(item.getProduct().getSeller().getId())) {
                    orderMoney.rentalRevenue = orderMoney.rentalRevenue.add(safe(item.getLineTotal()));
                    BigDecimal deposit = safe(item.getProduct().getDeposit())
                            .multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0L));
                    orderMoney.depositCollected = orderMoney.depositCollected.add(deposit);
                }
            }
            add(order, orderMoney);
        }

        void add(RentalOrder order, SellerMoneySummary orderMoney) {
            if (orderMoney.rentalRevenue.compareTo(BigDecimal.ZERO) == 0
                    && orderMoney.depositCollected.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            rentalRevenue = rentalRevenue.add(orderMoney.rentalRevenue);
            depositCollected = depositCollected.add(orderMoney.depositCollected);
            orderCount++;
            if (order.getStatus() == OrderStatus.COMPLETED) {
                completedOrderCount++;
            }
            if (ACTIVE_DEPOSIT_STATUSES.contains(order.getStatus())) {
                depositHeld = depositHeld.add(orderMoney.depositCollected);
            }
        }

        BigDecimal estimatedReceivable() {
            return rentalRevenue.add(depositCollected);
        }

        BigDecimal averageOrderValue() {
            if (orderCount == 0) {
                return BigDecimal.ZERO;
            }
            return rentalRevenue.divide(BigDecimal.valueOf(orderCount), 0, RoundingMode.HALF_UP);
        }
    }

    private static class RevenueBucket extends SellerMoneySummary {
        private final String key;
        private final String label;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;

        RevenueBucket(String key, String label, LocalDate periodStart, LocalDate periodEnd) {
            this.key = key;
            this.label = label;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }

        SellerRevenueResponse.Bar toBar() {
            return SellerRevenueResponse.Bar.builder()
                    .key(key)
                    .label(label)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .rentalRevenue(rentalRevenue)
                    .depositCollected(depositCollected)
                    .estimatedReceivable(estimatedReceivable())
                    .orderCount(orderCount)
                    .completedOrderCount(completedOrderCount)
                    .build();
        }
    }
}
