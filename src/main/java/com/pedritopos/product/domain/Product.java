package com.pedritopos.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.pedritopos.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {
    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 60)
    private String sku;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 8;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    @Column(nullable = false)
    private long version = 0;
}
