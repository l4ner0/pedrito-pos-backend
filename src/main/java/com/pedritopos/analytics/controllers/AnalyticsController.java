package com.pedritopos.analytics.controllers;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pedritopos.analytics.dto.response.SummaryResponse;
import com.pedritopos.analytics.services.AnalyticsService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getSummary(getBusinessId(authentication), date));
    }

    private UUID getBusinessId(Authentication authentication) {
        Claims claims = (Claims) authentication.getDetails();
        return UUID.fromString(claims.get("businessId", String.class));
    }
}
