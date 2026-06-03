package com.portfolio.siniestros.dto.request;

import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoPolizaRequest(
        @NotNull EstadoPoliza estado
) {}
