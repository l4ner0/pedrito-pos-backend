package com.pedritopos.analytics.dto.response;

import java.util.UUID;

public record StarProductResponse(
        UUID productId,
        String productName,
        long totalSold) {
}
