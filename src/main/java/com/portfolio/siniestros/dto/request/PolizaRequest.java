package com.portfolio.siniestros.dto.request;

import com.portfolio.siniestros.entity.enums.TipoPoliza;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaRequest(
        @NotNull TipoPoliza tipo,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaVencimiento,
        @NotNull @DecimalMin("0.01") BigDecimal coberturaMaxima,
        @DecimalMin("0.01") BigDecimal primaMensual,
        @Size(max = 500) String descripcion,
        @NotNull Long clienteId
) {}
