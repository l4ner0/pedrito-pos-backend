package com.pedritopos.business.controllers;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pedritopos.business.dto.request.BusinessPatchRequest;
import com.pedritopos.business.dto.request.BusinessRequest;
import com.pedritopos.business.dto.request.BusinessSettingsPatchRequest;
import com.pedritopos.business.dto.response.BusinessResponse;
import com.pedritopos.business.dto.response.BusinessSettingsResponse;
import com.pedritopos.business.services.BusinessService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<BusinessResponse> create(@Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(businessService.create(request));
    }

    @GetMapping
    public ResponseEntity<BusinessResponse> findOwn(Authentication authentication) {
        return ResponseEntity.ok(businessService.findById(getBusinessId(authentication)));
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessResponse> patch(Authentication authentication,
            @RequestBody BusinessPatchRequest request) {
        return ResponseEntity.ok(businessService.patch(getBusinessId(authentication), request));
    }

    @GetMapping("/settings")
    public ResponseEntity<BusinessSettingsResponse> findSettings(Authentication authentication) {
        return ResponseEntity.ok(businessService.findSettings(getBusinessId(authentication)));
    }

    @PatchMapping(value = "/settings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessSettingsResponse> patchSettings(
            Authentication authentication,
            @RequestParam(required = false) String yapeNumber,
            @RequestParam(required = false) String yapeQrUrl,
            @RequestParam(required = false) String yapeAccountHolder,
            @RequestParam(required = false) Boolean printEnabled,
            @RequestParam(required = false) String ticketFooter,
            @RequestParam(required = false) MultipartFile file) {
        BusinessSettingsPatchRequest request = new BusinessSettingsPatchRequest(
                yapeNumber, yapeQrUrl, yapeAccountHolder, printEnabled, ticketFooter);
        return ResponseEntity.ok(businessService.patchSettings(getBusinessId(authentication), request, file));
    }

    private UUID getBusinessId(Authentication authentication) {
        Claims claims = (Claims) authentication.getDetails();
        return UUID.fromString(claims.get("businessId", String.class));
    }
}
