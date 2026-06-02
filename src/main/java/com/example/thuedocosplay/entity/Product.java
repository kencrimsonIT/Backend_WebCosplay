package com.example.thuedocosplay.entity;

import jakarta.persistence.*;
import lombok.*;

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
}
