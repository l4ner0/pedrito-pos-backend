package com.pedritopos.product.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pedritopos.product.dto.request.ProductPatchRequest;
import com.pedritopos.product.dto.request.ProductRequest;
import com.pedritopos.product.dto.response.ProductResponse;
import com.pedritopos.product.services.ProductQueryService;
import com.pedritopos.product.services.ProductService;
import com.pedritopos.shared.dto.PagedResponse;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductQueryService productQueryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(Authentication authentication,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(getBusinessId(authentication), request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> findAll(Authentication authentication,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.findAll(getBusinessId(authentication), name, categoryName, page, size));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findByCategory(Authentication authentication,
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(productQueryService.findByCategory(getBusinessId(authentication), categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(getBusinessId(authentication), id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> patch(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody ProductPatchRequest request) {
        return ResponseEntity.ok(productService.patch(getBusinessId(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        productService.delete(getBusinessId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private UUID getBusinessId(Authentication authentication) {
        Claims claims = (Claims) authentication.getDetails();
        return UUID.fromString(claims.get("businessId", String.class));
    }
}
