package com.pedritopos.sale.repositories;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface TopProductProjection {
    UUID getId();
    String getName();
    String getSku();
    String getLogoUrl();
    UUID getCategoryId();
    BigDecimal getPrice();
    Integer getStock();
    Integer getLowStockThreshold();
    Boolean getActive();
    Instant getCreatedAt();
    Long getTotalSold();
}
