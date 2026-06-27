package com.pedritopos.sale.repositories;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pedritopos.sale.domain.Sale;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    Optional<Sale> findByIdAndBusinessId(UUID id, UUID businessId);

    @Query(value = "SELECT nextval('sale_ticket_seq')", nativeQuery = true)
    Long nextTicketNumber();

    @Query(value = """
            SELECT * FROM sales
            WHERE business_id = :businessId
              AND (:search IS NULL OR ticket_code ILIKE '%' || :search || '%'
                                   OR payment_method ILIKE '%' || :search || '%'
                                   OR status ILIKE '%' || :search || '%')
              AND (CAST(:from AS TIMESTAMPTZ) IS NULL OR created_at >= CAST(:from AS TIMESTAMPTZ))
              AND (CAST(:to AS TIMESTAMPTZ) IS NULL OR created_at <= CAST(:to AS TIMESTAMPTZ))
            ORDER BY created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM sales
            WHERE business_id = :businessId
              AND (:search IS NULL OR ticket_code ILIKE '%' || :search || '%'
                                   OR payment_method ILIKE '%' || :search || '%'
                                   OR status ILIKE '%' || :search || '%')
              AND (CAST(:from AS TIMESTAMPTZ) IS NULL OR created_at >= CAST(:from AS TIMESTAMPTZ))
              AND (CAST(:to AS TIMESTAMPTZ) IS NULL OR created_at <= CAST(:to AS TIMESTAMPTZ))
            """,
            nativeQuery = true)
    Page<Sale> findByBusiness(
            @Param("businessId") UUID businessId,
            @Param("search") String search,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
