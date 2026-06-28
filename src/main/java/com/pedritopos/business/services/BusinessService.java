package com.pedritopos.business.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pedritopos.business.domain.Business;
import com.pedritopos.business.domain.BusinessSettings;
import com.pedritopos.business.dto.request.BusinessPatchRequest;
import com.pedritopos.business.dto.request.BusinessRequest;
import com.pedritopos.business.dto.request.BusinessSettingsPatchRequest;
import com.pedritopos.business.dto.response.BusinessResponse;
import com.pedritopos.business.dto.response.BusinessSettingsResponse;
import com.pedritopos.business.repositories.BusinessRepository;
import com.pedritopos.business.repositories.BusinessSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessSettingsRepository businessSettingsRepository;

    @Transactional
    public BusinessResponse create(BusinessRequest request) {
        Business business = new Business();
        business.setName(request.name().trim());
        business.setRuc(request.ruc() != null && !request.ruc().isBlank() ? request.ruc().trim() : null);
        business.setAddress(request.address() != null && !request.address().isBlank() ? request.address().trim() : null);
        business.setPhone(request.phone() != null && !request.phone().isBlank() ? request.phone().trim() : null);
        business = businessRepository.save(business);

        BusinessSettings settings = new BusinessSettings();
        settings.setBusinessId(business.getId());
        settings.setPrintEnabled(false);
        businessSettingsRepository.save(settings);

        return toResponse(business);
    }

    public BusinessResponse findById(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));
        return toResponse(business);
    }

    @Transactional
    public BusinessResponse patch(UUID businessId, BusinessPatchRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        if (request.name() != null && !request.name().isBlank()) {
            business.setName(request.name().trim());
        }
        if (request.ruc() != null) {
            business.setRuc(request.ruc().isBlank() ? null : request.ruc().trim());
        }
        if (request.address() != null) {
            business.setAddress(request.address().isBlank() ? null : request.address().trim());
        }
        if (request.phone() != null) {
            business.setPhone(request.phone().isBlank() ? null : request.phone().trim());
        }

        return toResponse(businessRepository.save(business));
    }

    public BusinessSettingsResponse findSettings(UUID businessId) {
        return businessSettingsRepository.findByBusinessId(businessId)
                .map(this::toSettingsResponse)
                .orElse(new BusinessSettingsResponse(null, businessId, null, null, null, false, null));
    }

    @Transactional
    public BusinessSettingsResponse patchSettings(UUID businessId, BusinessSettingsPatchRequest request) {
        BusinessSettings settings = businessSettingsRepository.findByBusinessId(businessId)
                .orElseGet(() -> {
                    BusinessSettings s = new BusinessSettings();
                    s.setBusinessId(businessId);
                    return s;
                });

        if (request.yapeNumber() != null) {
            settings.setYapeNumber(request.yapeNumber().isBlank() ? null : request.yapeNumber().trim());
        }
        if (request.yapeQrUrl() != null) {
            settings.setYapeQrUrl(request.yapeQrUrl().isBlank() ? null : request.yapeQrUrl().trim());
        }
        if (request.yapeAccountHolder() != null) {
            settings.setYapeAccountHolder(request.yapeAccountHolder().isBlank() ? null : request.yapeAccountHolder().trim());
        }
        if (request.printEnabled() != null) {
            settings.setPrintEnabled(request.printEnabled());
        }
        if (request.ticketFooter() != null) {
            settings.setTicketFooter(request.ticketFooter().isBlank() ? null : request.ticketFooter().trim());
        }

        return toSettingsResponse(businessSettingsRepository.save(settings));
    }

    private BusinessResponse toResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getName(),
                business.getRuc(),
                business.getAddress(),
                business.getPhone(),
                business.getCreatedAt());
    }

    private BusinessSettingsResponse toSettingsResponse(BusinessSettings settings) {
        return new BusinessSettingsResponse(
                settings.getId(),
                settings.getBusinessId(),
                settings.getYapeNumber(),
                settings.getYapeQrUrl(),
                settings.getYapeAccountHolder(),
                settings.isPrintEnabled(),
                settings.getTicketFooter());
    }
}
