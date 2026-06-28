package com.pedritopos.business.dto.request;

public record BusinessSettingsPatchRequest(
        String yapeNumber,
        String yapeQrUrl,
        String yapeAccountHolder,
        Boolean printEnabled,
        String ticketFooter) {
}
