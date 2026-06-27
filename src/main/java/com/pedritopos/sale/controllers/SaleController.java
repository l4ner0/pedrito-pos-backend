package com.pedritopos.sale.controllers;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pedritopos.sale.dto.request.SaleRequest;
import com.pedritopos.sale.dto.response.SaleResponse;
import com.pedritopos.sale.services.SaleService;
import com.pedritopos.shared.dto.PagedResponse;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> create(Authentication authentication,
            @Valid @RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.create(getBusinessId(authentication), getUserId(authentication), request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<SaleResponse>> findAll(Authentication authentication,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                saleService.findAll(getBusinessId(authentication), paymentMethod, status, from, to, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> findById(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(saleService.findById(getBusinessId(authentication), id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> cancel(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(saleService.cancel(getBusinessId(authentication), id));
    }

    private UUID getBusinessId(Authentication authentication) {
        Claims claims = (Claims) authentication.getDetails();
        return UUID.fromString(claims.get("businessId", String.class));
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
