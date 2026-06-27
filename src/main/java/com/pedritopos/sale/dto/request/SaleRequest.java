package com.pedritopos.sale.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record SaleRequest(
        @NotEmpty @Valid List<SaleItemRequest> items,
        @DecimalMin("0.0") BigDecimal discountAmount,
        @NotBlank @Pattern(regexp = "EFECTIVO|YAPE", message = "Debe ser EFECTIVO o YAPE") String paymentMethod,
        BigDecimal amountReceived) {
}
