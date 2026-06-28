package com.pedritopos.sale.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TopProductResponse(
        UUID id,
        String name,
        String sku,
        String logoUrl,
        UUID categoryId,
        BigDecimal price,
        int stock,
        int lowStockThreshold,
        boolean active,
        boolean lowStock,
        Instant createdAt,
        long totalSold) {
}
