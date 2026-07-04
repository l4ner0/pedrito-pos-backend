package com.pedritopos.analytics.dto.response;

import java.math.BigDecimal;

public record SummaryResponse(
        BigDecimal totalRevenue,
        long salesCount,
        BigDecimal averageTicket,
        StarProductResponse starProduct) {
}
