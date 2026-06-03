package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import com.portfolio.siniestros.entity.enums.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PolizaResponse(
        Long id,
        String numeroPoliza,
        TipoPoliza tipo,
        EstadoPoliza estado,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        BigDecimal coberturaMaxima,
        BigDecimal primaMensual,
        String descripcion,
        ClienteResumenResponse cliente,
        List<SiniestroResumenResponse> siniestros,
        LocalDateTime createdAt
) {}
