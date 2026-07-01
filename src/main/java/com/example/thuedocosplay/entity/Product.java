package com.example.thuedocosplay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
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

    @Column(name = "price_per_day", nullable = false, precision = 15, scale = 0)
    private BigDecimal pricePerDay;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal deposit;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean visible = true;

    private String description;

    @Formula("(SELECT COALESCE(AVG(r.rating), 0) FROM reviews r WHERE r.product_id = id)")
    private Double rating;

    @Formula("(SELECT COUNT(r.id) FROM reviews r WHERE r.product_id = id)")
    private Integer reviewCount;
}
