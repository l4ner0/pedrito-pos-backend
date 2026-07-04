package com.pedritopos.analytics.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pedritopos.analytics.dto.response.StarProductResponse;
import com.pedritopos.analytics.dto.response.SummaryResponse;
import com.pedritopos.analytics.repositories.AnalyticsRepository;
import com.pedritopos.analytics.repositories.SalesSummaryProjection;
import com.pedritopos.analytics.repositories.StarProductProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public SummaryResponse getSummary(UUID businessId, LocalDate date) {
        Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        SalesSummaryProjection stats = analyticsRepository.getSummary(businessId, from, to);
        Optional<StarProductProjection> starProduct = analyticsRepository.findStarProduct(businessId, from, to);

        long salesCount = stats.getSalesCount();
        BigDecimal totalRevenue = stats.getTotalRevenue();
        BigDecimal averageTicket = salesCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        StarProductResponse starProductResponse = starProduct
                .map(p -> new StarProductResponse(p.getProductId(), p.getProductName(), p.getTotalSold()))
                .orElse(null);

        return new SummaryResponse(totalRevenue, salesCount, averageTicket, starProductResponse);
    }
}
