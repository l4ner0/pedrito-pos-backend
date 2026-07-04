package com.pedritopos.analytics.repositories;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pedritopos.sale.domain.Sale;

public interface AnalyticsRepository extends JpaRepository<Sale, UUID> {

    @Query(value = """
            SELECT
                COALESCE(SUM(total), 0) AS total_revenue,
                COUNT(*)                AS sales_count
            FROM sales
            WHERE business_id = :businessId
              AND status     = 'ACTIVE'
              AND created_at >= CAST(:from AS TIMESTAMPTZ)
              AND created_at <  CAST(:to   AS TIMESTAMPTZ)
            """, nativeQuery = true)
    SalesSummaryProjection getSummary(
            @Param("businessId") UUID businessId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query(value = """
            SELECT
                p.id             AS product_id,
                p.name           AS product_name,
                SUM(si.quantity) AS total_sold
            FROM sale_items si
            JOIN sales    s ON si.sale_id    = s.id
            JOIN products p ON si.product_id = p.id
            WHERE s.business_id = :businessId
              AND s.status      = 'ACTIVE'
              AND s.created_at >= CAST(:from AS TIMESTAMPTZ)
              AND s.created_at <  CAST(:to   AS TIMESTAMPTZ)
            GROUP BY p.id, p.name
            ORDER BY total_sold DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<StarProductProjection> findStarProduct(
            @Param("businessId") UUID businessId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
