package com.pedritopos.auth.dto.response;

public record LoginResponse(
                String token,
                String fullName,
                String role) {

}
