package com.pedritopos.category.dto.response;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        boolean active) {
}