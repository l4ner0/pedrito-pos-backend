package com.pedritopos.sale.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedritopos.sale.domain.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    List<SaleItem> findBySaleId(UUID saleId);
}
