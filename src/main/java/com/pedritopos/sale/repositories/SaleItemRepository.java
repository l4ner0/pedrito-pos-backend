package com.pedritopos.sale.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pedritopos.sale.domain.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    List<SaleItem> findBySaleId(UUID saleId);

    @Query(value = """
            SELECT
                p.id                  AS id,
                p.name                AS name,
                p.sku                 AS sku,
                p.logo_url            AS logo_url,
                p.category_id         AS category_id,
                p.price               AS price,
                p.stock               AS stock,
                p.low_stock_threshold AS low_stock_threshold,
                p.active              AS active,
                p.created_at          AS created_at,
                COALESCE(SUM(si.quantity), 0) AS total_sold
            FROM products p
            LEFT JOIN sale_items si ON si.product_id = p.id
            LEFT JOIN sales s ON si.sale_id = s.id
                              AND s.business_id = :businessId
                              AND s.status = 'ACTIVE'
            WHERE p.business_id = :businessId
              AND p.active = true
            GROUP BY p.id, p.name, p.sku, p.logo_url, p.category_id,
                     p.price, p.stock, p.low_stock_threshold, p.active, p.created_at
            ORDER BY total_sold DESC, p.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            WHERE p.business_id = :businessId
              AND p.active = true
            """,
            nativeQuery = true)
    Page<TopProductProjection> findTopProducts(@Param("businessId") UUID businessId, Pageable pageable);
}
