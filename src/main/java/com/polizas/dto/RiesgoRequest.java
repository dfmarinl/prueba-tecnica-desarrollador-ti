package com.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RiesgoRequest(
        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 200, message = "La descripcion no puede exceder 200 caracteres")
        String descripcion) {
}
