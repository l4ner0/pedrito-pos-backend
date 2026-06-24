package com.pedritopos.category.controllers;

import com.pedritopos.category.dto.request.CategoryRequest;
import com.pedritopos.category.dto.response.CategoryResponse;
import com.pedritopos.category.services.CategoryService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(Authentication authentication,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(getBusinessId(authentication), request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(categoryService.findAll(getBusinessId(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(getBusinessId(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID getBusinessId(Authentication authentication) {
        Claims claims = (Claims) authentication.getDetails();
        return UUID.fromString(claims.get("businessId", String.class));
    }
}