package com.pedritopos.business.dto.request;

public record BusinessSettingsPatchRequest(
        String yapeNumber,
        String yapeQrUrl,
        Boolean printEnabled,
        String ticketFooter) {
}
