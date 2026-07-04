package com.pedritopos.analytics.repositories;

import java.util.UUID;

public interface StarProductProjection {
    UUID getProductId();
    String getProductName();
    Long getTotalSold();
}
