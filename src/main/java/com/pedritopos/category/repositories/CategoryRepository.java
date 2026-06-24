package com.pedritopos.category.repositories;

import com.pedritopos.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE c.businessId = :businessId AND c.active = true")
    List<Category> findActiveByBusiness(@Param("businessId") UUID businessId);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.businessId = :businessId AND LOWER(c.name) = LOWER(:name)")
    boolean existsDuplicate(@Param("businessId") UUID businessId, @Param("name") String name);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.businessId = :businessId AND LOWER(c.name) = LOWER(:name) AND c.id <> :id")
    boolean existsDuplicateExcluding(@Param("businessId") UUID businessId, @Param("name") String name,
            @Param("id") UUID id);

}
