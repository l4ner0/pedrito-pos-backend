package com.pedritopos.product.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String sku,
        UUID categoryId,
        BigDecimal price,
        int stock,
        int lowStockThreshold,
        boolean active,
        boolean lowStock,
        Instant createdAt) {
}
