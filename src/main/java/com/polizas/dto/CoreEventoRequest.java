package com.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CoreEventoRequest(
        @NotBlank(message = "El evento es obligatorio")
        String evento,

        @NotNull(message = "El polizaId es obligatorio")
        Long polizaId) {
}
