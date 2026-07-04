package com.pedritopos.analytics.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pedritopos.product.domain.Product;

public interface ProductAnalyticsRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.active = true AND p.stock < p.lowStockThreshold ORDER BY p.stock ASC")
    List<Product> findLowStock(@Param("businessId") UUID businessId, Pageable pageable);
}
