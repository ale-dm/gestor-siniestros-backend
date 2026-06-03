package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.EstadoSiniestro;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SiniestroResumenResponse(
        Long id,
        String numeroSiniestro,
        EstadoSiniestro estado,
        LocalDateTime fechaApertura,
        BigDecimal importeReclamado
) {}
