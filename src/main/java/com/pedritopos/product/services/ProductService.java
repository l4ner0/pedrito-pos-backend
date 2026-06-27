package com.pedritopos.product.services;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pedritopos.product.domain.Product;
import com.pedritopos.product.dto.request.ProductPatchRequest;
import com.pedritopos.product.dto.request.ProductRequest;
import com.pedritopos.product.dto.response.ProductResponse;
import com.pedritopos.product.repositories.ProductRepository;
import com.pedritopos.shared.dto.PagedResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(UUID businessId, ProductRequest request) {
        if (hasSku(request.sku()) && productRepository.existsActiveBySku(businessId, request.sku())) {
            throw new RuntimeException("Ya existe un producto con ese SKU");
        }

        Product product = new Product();
        product.setBusinessId(businessId);
        applyRequest(product, request);
        productRepository.save(product);
        return toResponse(product);
    }

    public PagedResponse<ProductResponse> findAll(UUID businessId, String name, String categoryName, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return PagedResponse.from(
                productRepository.search(businessId, blankToNull(name), blankToNull(categoryName), pageable)
                        .map(this::toResponse));
    }

    public ProductResponse findById(UUID businessId, UUID id) {
        return toResponse(findActive(businessId, id));
    }

    @Transactional
    public ProductResponse patch(UUID businessId, UUID id, ProductPatchRequest request) {
        Product product = findActive(businessId, id);

        if (hasSku(request.sku()) && productRepository.existsActiveBySkuExcluding(businessId, request.sku(), id)) {
            throw new RuntimeException("Ya existe un producto con ese SKU");
        }

        if (request.name() != null && !request.name().isBlank()) product.setName(request.name());
        if (request.categoryId() != null) product.setCategoryId(request.categoryId());
        if (request.sku() != null) product.setSku(hasSku(request.sku()) ? request.sku() : null);
        if (request.price() != null) product.setPrice(request.price());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.lowStockThreshold() != null) product.setLowStockThreshold(request.lowStockThreshold());

        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void delete(UUID businessId, UUID id) {
        Product product = findActive(businessId, id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findActive(UUID businessId, UUID id) {
        return productRepository.findByIdAndBusinessId(id, businessId)
                .filter(Product::isActive)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setCategoryId(request.categoryId());
        product.setSku(hasSku(request.sku()) ? request.sku() : null);
        product.setPrice(request.price());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.lowStockThreshold() != null) product.setLowStockThreshold(request.lowStockThreshold());
    }

    private boolean hasSku(String sku) {
        return sku != null && !sku.isBlank();
    }

    private String blankToNull(String value) {
        return (value != null && !value.isBlank()) ? value.trim() : null;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getCategoryId(),
                product.getPrice(),
                product.getStock(),
                product.getLowStockThreshold(),
                product.isActive(),
                product.getStock() < product.getLowStockThreshold(),
                product.getCreatedAt());
    }
}
