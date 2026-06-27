package com.pedritopos.product.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotBlank String name,
        UUID categoryId,
        String sku,
        String logoUrl,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(0) Integer stock,
        @Min(0) Integer lowStockThreshold) {
}
