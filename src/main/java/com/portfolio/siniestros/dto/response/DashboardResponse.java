package com.portfolio.siniestros.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long abiertos,
        long enPeritacion,
        long resueltos,
        long denegados,
        BigDecimal totalImporteIndemnizado,
        Double mediaDiasResolucion,
        Map<String, Long> distribucionPorTipo,
        List<SiniestroResumenResponse> ultimosAbiertos
) {}
