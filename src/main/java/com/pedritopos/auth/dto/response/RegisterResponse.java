package com.pedritopos.auth.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String fullName,
        String email,
        String role) {
}