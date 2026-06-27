package com.pedritopos.product.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record ProductPatchRequest(
        String name,
        UUID categoryId,
        String sku,
        String logoUrl,
        @DecimalMin("0.0") BigDecimal price,
        @Min(0) Integer stock,
        @Min(0) Integer lowStockThreshold) {
}
