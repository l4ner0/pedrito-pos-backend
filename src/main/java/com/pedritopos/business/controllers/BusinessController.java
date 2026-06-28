package com.pedritopos.business.controllers;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(businessService.findById(id));
    }

    @GetMapping("/settings")
    public ResponseEntity<BusinessSettingsResponse> findSettings(Authentication authentication) {
        return ResponseEntity.ok(businessService.findSettings(getBusinessId(authentication)));
    }

    @PatchMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessSettingsResponse> patchSettings(Authentication authentication,
            @RequestBody BusinessSettingsPatchRequest request) {
        return ResponseEntity.ok(businessService.patchSettings(getBusinessId(authentication), request));
    }

    private UUID getBusinessId(Authentication authentication) {
        Claims claims = (Claims) authentication.getDetails();
        return UUID.fromString(claims.get("businessId", String.class));
    }
}
