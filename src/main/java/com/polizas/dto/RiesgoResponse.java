package com.polizas.dto;

import com.polizas.entity.EstadoRiesgo;

public record RiesgoResponse(
        Long id,
        Long polizaId,
        EstadoRiesgo estado,
        String descripcion) {
}
