package com.pedritopos.product.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pedritopos.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.active = true ORDER BY p.name")
    List<Product> findActiveByBusiness(@Param("businessId") UUID businessId);

    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    @Query(value = "SELECT * FROM products WHERE business_id = :businessId AND active = true AND name ILIKE '%' || :name || '%' ORDER BY name",
            nativeQuery = true)
    List<Product> searchByName(@Param("businessId") UUID businessId, @Param("name") String name);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.businessId = :businessId AND p.sku = :sku AND p.active = true")
    boolean existsActiveBySku(@Param("businessId") UUID businessId, @Param("sku") String sku);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.businessId = :businessId AND p.sku = :sku AND p.active = true AND p.id <> :id")
    boolean existsActiveBySkuExcluding(@Param("businessId") UUID businessId, @Param("sku") String sku,
            @Param("id") UUID id);
}
