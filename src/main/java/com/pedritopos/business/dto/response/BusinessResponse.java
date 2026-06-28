package com.pedritopos.business.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BusinessResponse(
        UUID id,
        String name,
        String ruc,
        String address,
        String phone,
        Instant createdAt) {
}
