package com.pedritopos.product.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pedritopos.product.dto.response.ProductResponse;
import com.pedritopos.product.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductQueryService {
    private final ProductRepository productRepository;

    public List<ProductResponse> findByCategory(UUID businessId, UUID categoryId) {
        return productRepository.findActiveByCategory(businessId, categoryId)
                .stream()
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getSku(),
                        p.getLogoUrl(),
                        p.getCategoryId(),
                        p.getPrice(),
                        p.getStock(),
                        p.getLowStockThreshold(),
                        p.isActive(),
                        p.getStock() < p.getLowStockThreshold(),
                        p.getCreatedAt()))
                .toList();
    }
}
