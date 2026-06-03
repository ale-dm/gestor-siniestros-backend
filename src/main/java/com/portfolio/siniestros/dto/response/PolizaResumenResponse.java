package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import com.portfolio.siniestros.entity.enums.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResumenResponse(
        Long id,
        String numeroPoliza,
        TipoPoliza tipo,
        EstadoPoliza estado,
        LocalDate fechaVencimiento,
        BigDecimal coberturaMaxima
) {}
