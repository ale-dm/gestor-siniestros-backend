package com.portfolio.siniestros.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SiniestroRequest(
        @NotBlank @Size(max = 1000) String descripcion,
        @DecimalMin("0.01") BigDecimal importeReclamado,
        @NotNull Long polizaId
) {}
