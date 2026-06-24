package com.pedritopos.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

                @NotBlank(message = "El nombre es requerido") @Size(max = 100, message = "El nombre no puede superar los 100 caracteres") String name) {
}