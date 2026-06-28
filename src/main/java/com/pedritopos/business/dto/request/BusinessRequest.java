package com.pedritopos.business.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BusinessRequest(
        @NotBlank String name,
        String ruc,
        String address,
        String phone) {
}
