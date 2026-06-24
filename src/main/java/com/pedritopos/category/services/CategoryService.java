package com.pedritopos.category.services;

import com.pedritopos.category.domain.Category;
import com.pedritopos.category.dto.request.CategoryRequest;
import com.pedritopos.category.dto.response.CategoryResponse;
import com.pedritopos.category.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponse create(UUID businessId, CategoryRequest request) {
        if (categoryRepository.existsDuplicate(businessId, request.name().toLowerCase())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        Category category = new Category();
        category.setBusinessId(businessId);
        category.setName(request.name().toLowerCase());
        categoryRepository.save(category);
        return toResponse(category);
    }

    public List<CategoryResponse> findAll(UUID businessId) {
        return categoryRepository.findActiveByBusiness(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse update(UUID businessId, UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (categoryRepository.existsDuplicateExcluding(businessId, request.name(), id)) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        category.setName(request.name().toLowerCase());
        categoryRepository.save(category);
        return toResponse(category);
    }

    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.isActive());
    }
}
