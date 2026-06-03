package com.portfolio.siniestros.dto.request;

import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CambioEstadoRequest(
        @NotNull EstadoSiniestro estado,
        BigDecimal importeIndemnizado,
        String observaciones
) {}
