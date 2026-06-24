package com.pedritopos.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String fullName,
        String role) {

}
