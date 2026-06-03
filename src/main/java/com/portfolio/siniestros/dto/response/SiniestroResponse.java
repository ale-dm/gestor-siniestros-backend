package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.EstadoSiniestro;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SiniestroResponse(
        Long id,
        String numeroSiniestro,
        String descripcion,
        EstadoSiniestro estado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaResolucion,
        BigDecimal importeReclamado,
        BigDecimal importeIndemnizado,
        String observaciones,
        PolizaResumenResponse poliza,
        UsuarioResumenResponse perito,
        LocalDateTime createdAt
) {}
