package com.pedritopos.auth.dto.response;

public record LoginResponse(
                String username,
                String accessToken,
                String refreshToken,
                String fullName,
                String role) {

}
