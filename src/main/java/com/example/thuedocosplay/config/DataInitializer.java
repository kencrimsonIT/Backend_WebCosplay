package com.example.thuedocosplay.config;

import com.example.thuedocosplay.entity.*;
import com.example.thuedocosplay.entity.enums.*;
import com.example.thuedocosplay.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RentalOrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category anime = categoryRepository.save(Category.builder().name("Anime").active(true).build());
        Category game = categoryRepository.save(Category.builder().name("Game").active(true).build());
        Category hero = categoryRepository.save(Category.builder().name("Siêu Anh Hùng").active(true).build());
        Category fantasy = categoryRepository.save(Category.builder().name("Fantasy").active(true).build());

        Product p1 = productRepository.save(Product.builder()
                .name("Trang phục Zenitsu Agatsuma")
                .category(anime)
                .pricePerDay(new BigDecimal("120000"))
                .deposit(new BigDecimal("200000"))
                .imageUrl("https://myjapanclothes.com/cdn/shop/files/zenitsu-agatsuma-cosplay-demon-slayer_1_grande.jpg?v=1700752723")
                .visible(true)
                .build());

        Product p2 = productRepository.save(Product.builder()
                .name("Trang phục Sailor Moon")
                .category(anime)
                .pricePerDay(new BigDecimal("150000"))
                .deposit(new BigDecimal("250000"))
                .imageUrl("https://m.media-amazon.com/images/I/71XTNC3EKsL._AC_UY1000_.jpg")
                .visible(true)
                .build());

        Product p3 = productRepository.save(Product.builder()
                .name("Trang phục Batman")
                .category(hero)
                .pricePerDay(new BigDecimal("180000"))
                .deposit(new BigDecimal("300000"))
                .imageUrl("https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600")
                .visible(true)
                .build());

        User admin = userRepository.save(User.builder()
                .fullName("Admin Cosplay")
                .email("admin@cosplay.vn")
                .phone("0900000000")
                .password(passwordEncoder.encode("Admin@123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build());

        User seller = userRepository.save(User.builder()
                .fullName("Shop Sakura")
                .email("sakura@cosplay.vn")
                .phone("0988777666")
                .password(passwordEncoder.encode("Seller@123"))
                .role(UserRole.SELLER)
                .enabled(true)
                .build());

        User client = userRepository.save(User.builder()
                .fullName("Nguyễn Minh Tuấn")
                .email("minhtuan@gmail.com")
                .phone("0912345678")
                .password(passwordEncoder.encode("Client@123"))
                .role(UserRole.CLIENT)
                .enabled(true)
                .build());

        p1.setSeller(seller);
        p1.setQuantity(3);
        p2.setSeller(seller);
        p2.setQuantity(2);
        p3.setSeller(seller);
        p3.setQuantity(1);
        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        YearMonth current = YearMonth.now();
        LocalDateTime paidAt = current.atDay(5).atTime(10, 30);

        seedPaidOrder("ORD-SEED-001", p1, anime.getName(), paidAt, new BigDecimal("480000"));
        seedPaidOrder("ORD-SEED-002", p2, anime.getName(), paidAt.plusDays(2), new BigDecimal("750000"));
        seedPaidOrder("ORD-SEED-003", p3, hero.getName(), paidAt.plusDays(4), new BigDecimal("540000"));
        seedPaidOrder("ORD-SEED-004", p1, anime.getName(), paidAt.plusDays(7), new BigDecimal("360000"));
        seedClientOrder(client, "ORD-MINHTUAN-001", p1, anime.getName(), paidAt.plusDays(8));

        RentalOrder gameOrder = RentalOrder.builder()
                .orderCode("ORD-SEED-005")
                .customerName("Lê Hoàng Nam")
                .customerPhone("0901222333")
                .customerEmail("namle@gmail.com")
                .shippingAddress("88 Lê Lợi, Q.3, TP.HCM")
                .status(OrderStatus.COMPLETED)
                .paymentMethod(PaymentMethod.VNPAY)
                .rentalTotal(new BigDecimal("600000"))
                .warrantyTotal(BigDecimal.ZERO)
                .depositTotal(new BigDecimal("200000"))
                .grandTotal(new BigDecimal("800000"))
                .rentFrom(current.atDay(10))
                .rentTo(current.atDay(14))
                .paidAt(paidAt.plusDays(10))
                .build();

        OrderItem gameItem = OrderItem.builder()
                .order(gameOrder)
                .product(p3)
                .productName("Elden Ring Cosplay")
                .categoryName(game.getName())
                .size("L")
                .days(4)
                .quantity(1)
                .lineTotal(new BigDecimal("600000"))
                .build();
        gameOrder.getItems().add(gameItem);
        orderRepository.save(gameOrder);

        RentalOrder fantasyOrder = RentalOrder.builder()
                .orderCode("ORD-SEED-006")
                .customerName("Phạm Gia Hân")
                .customerPhone("0933444555")
                .customerEmail("han@gmail.com")
                .shippingAddress("12 Pasteur, Q.1, TP.HCM")
                .status(OrderStatus.COMPLETED)
                .paymentMethod(PaymentMethod.MOMO)
                .rentalTotal(new BigDecimal("420000"))
                .warrantyTotal(new BigDecimal("50000"))
                .depositTotal(new BigDecimal("150000"))
                .grandTotal(new BigDecimal("620000"))
                .rentFrom(current.atDay(12))
                .rentTo(current.atDay(15))
                .paidAt(paidAt.plusDays(12))
                .build();

        OrderItem fantasyItem = OrderItem.builder()
                .order(fantasyOrder)
                .product(p2)
                .productName("Fantasy Elf Queen")
                .categoryName(fantasy.getName())
                .size("M")
                .days(3)
                .quantity(1)
                .lineTotal(new BigDecimal("470000"))
                .build();
        fantasyOrder.getItems().add(fantasyItem);
        orderRepository.save(fantasyOrder);
    }

    private void seedPaidOrder(String code, Product product, String categoryName,
                               LocalDateTime paidAt, BigDecimal lineTotal) {
        RentalOrder order = RentalOrder.builder()
                .orderCode(code)
                .customerName("Khách demo")
                .customerPhone("0909999888")
                .customerEmail("demo@cosplay.vn")
                .shippingAddress("123 Nguyễn Huệ, Q.1, TP.HCM")
                .status(OrderStatus.COMPLETED)
                .paymentMethod(PaymentMethod.VNPAY)
                .rentalTotal(lineTotal)
                .warrantyTotal(BigDecimal.ZERO)
                .depositTotal(product.getDeposit())
                .grandTotal(lineTotal.add(product.getDeposit()))
                .rentFrom(paidAt.toLocalDate())
                .rentTo(paidAt.toLocalDate().plusDays(3))
                .paidAt(paidAt)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .categoryName(categoryName)
                .size("M")
                .days(3)
                .quantity(1)
                .lineTotal(lineTotal)
                .build();
        order.getItems().add(item);
        orderRepository.save(order);
    }

    private void seedClientOrder(User client, String code, Product product, String categoryName, LocalDateTime paidAt) {
        BigDecimal rentalTotal = product.getPricePerDay().multiply(new BigDecimal("3"));
        BigDecimal warrantyTotal = new BigDecimal("50000");
        BigDecimal depositTotal = product.getDeposit();

        RentalOrder order = RentalOrder.builder()
                .orderCode(code)
                .customer(client)
                .customerName(client.getFullName())
                .customerPhone(client.getPhone())
                .customerEmail(client.getEmail())
                .shippingAddress("25 Nguyen Trai, Q.5, TP.HCM")
                .status(OrderStatus.COMPLETED)
                .paymentMethod(PaymentMethod.VNPAY)
                .rentalTotal(rentalTotal)
                .warrantyTotal(warrantyTotal)
                .depositTotal(depositTotal)
                .grandTotal(rentalTotal.add(warrantyTotal).add(depositTotal))
                .rentFrom(paidAt.toLocalDate().minusDays(5))
                .rentTo(paidAt.toLocalDate().minusDays(2))
                .paidAt(paidAt)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .categoryName(categoryName)
                .size("M")
                .days(3)
                .quantity(1)
                .lineTotal(rentalTotal)
                .build();

        order.getItems().add(item);
        orderRepository.save(order);
    }
}
