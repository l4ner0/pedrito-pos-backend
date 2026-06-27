package com.pedritopos.product.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pedritopos.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = """
            SELECT p.* FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.business_id = :businessId
              AND p.active = true
              AND (:name IS NULL OR p.name ILIKE '%' || :name || '%')
              AND (:categoryName IS NULL OR c.name ILIKE '%' || :categoryName || '%')
            ORDER BY p.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(p.id) FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.business_id = :businessId
              AND p.active = true
              AND (:name IS NULL OR p.name ILIKE '%' || :name || '%')
              AND (:categoryName IS NULL OR c.name ILIKE '%' || :categoryName || '%')
            """,
            nativeQuery = true)
    Page<Product> search(@Param("businessId") UUID businessId,
            @Param("name") String name,
            @Param("categoryName") String categoryName,
            Pageable pageable);

    @Query(value = """
            SELECT p.* FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.business_id = :businessId
              AND p.active = true
              AND (:name IS NULL OR p.name ILIKE '%' || :name || '%')
              AND (:categoryName IS NULL OR c.name ILIKE '%' || :categoryName || '%')
            ORDER BY p.created_at DESC
            """, nativeQuery = true)
    List<Product> searchAll(@Param("businessId") UUID businessId,
            @Param("name") String name,
            @Param("categoryName") String categoryName);

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.categoryId = :categoryId AND p.active = true ORDER BY p.createdAt DESC")
    List<Product> findActiveByCategory(@Param("businessId") UUID businessId, @Param("categoryId") UUID categoryId);

    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.businessId = :businessId AND p.sku = :sku AND p.active = true")
    boolean existsActiveBySku(@Param("businessId") UUID businessId, @Param("sku") String sku);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.businessId = :businessId AND p.sku = :sku AND p.active = true AND p.id <> :id")
    boolean existsActiveBySkuExcluding(@Param("businessId") UUID businessId, @Param("sku") String sku,
            @Param("id") UUID id);
}
