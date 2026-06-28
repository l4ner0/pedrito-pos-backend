package com.pedritopos.business.dto.response;

import java.util.UUID;

public record BusinessSettingsResponse(
        UUID id,
        UUID businessId,
        String yapeNumber,
        String yapeQrUrl,
        String yapeAccountHolder,
        boolean printEnabled,
        String ticketFooter) {
}
