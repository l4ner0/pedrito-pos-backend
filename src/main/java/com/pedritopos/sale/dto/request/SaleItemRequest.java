package com.pedritopos.sale.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaleItemRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity) {
}
