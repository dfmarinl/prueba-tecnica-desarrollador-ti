package com.polizas.dto;

import com.polizas.entity.EstadoPoliza;
import com.polizas.entity.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id,
        TipoPoliza tipo,
        EstadoPoliza estado,
        BigDecimal canon,
        Integer meses,
        BigDecimal prima,
        LocalDate fechaInicio) {
}
