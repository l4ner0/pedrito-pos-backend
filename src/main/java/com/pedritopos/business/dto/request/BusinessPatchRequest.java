package com.pedritopos.business.dto.request;

public record BusinessPatchRequest(
        String name,
        String ruc,
        String address,
        String phone) {
}
