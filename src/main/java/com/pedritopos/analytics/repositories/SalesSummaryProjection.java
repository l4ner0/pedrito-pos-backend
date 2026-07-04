package com.pedritopos.analytics.repositories;

import java.math.BigDecimal;

public interface SalesSummaryProjection {
    BigDecimal getTotalRevenue();
    Long getSalesCount();
}
