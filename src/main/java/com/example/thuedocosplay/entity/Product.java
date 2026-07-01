package com.example.thuedocosplay.entity;

import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(length = 2000)
    private String description;

    @Column(name = "price_per_day", nullable = false, precision = 15, scale = 0)
    private BigDecimal pricePerDay;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal deposit;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean visible = true;

    // ─────────────────────────────────────────────────────────────────────────
    // CÁC TRƯỜNG QUẢN LÝ TỒN KHO & AUDITING (Từ nhánh main)
    // ─────────────────────────────────────────────────────────────────────────
    @Column(nullable = false, columnDefinition = "integer default 1")
    @Builder.Default
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false, columnDefinition = "varchar(255) default 'AVAILABLE'")
    @Builder.Default
    private ProductInventoryStatus inventoryStatus = ProductInventoryStatus.AVAILABLE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // CÁC TRƯỜNG TỐI ƯU HIỆU NĂNG (Từ nhánh của bạn)
    // ─────────────────────────────────────────────────────────────────────────
    @Formula("(SELECT COALESCE(AVG(r.rating), 0) FROM reviews r WHERE r.product_id = id)")
    private Double rating;

    @Formula("(SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = id)")
    private Integer reviewCount;
}