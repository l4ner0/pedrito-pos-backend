package com.pedritopos.business.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedritopos.business.domain.BusinessSettings;

public interface BusinessSettingsRepository extends JpaRepository<BusinessSettings, UUID> {
    Optional<BusinessSettings> findByBusinessId(UUID businessId);
}
