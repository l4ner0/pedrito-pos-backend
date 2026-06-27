package com.pedritopos.sale.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID userId,
        String ticketCode,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal total,
        String paymentMethod,
        BigDecimal amountReceived,
        BigDecimal changeGiven,
        List<SaleItemResponse> items,
        String status,
        Instant createdAt) {
}
