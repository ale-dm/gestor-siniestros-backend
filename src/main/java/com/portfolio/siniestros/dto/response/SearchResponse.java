package com.portfolio.siniestros.dto.response;

import java.util.List;

public record SearchResponse(
        List<ClienteResumenResponse> clientes,
        List<PolizaResumenResponse> polizas,
        List<SiniestroResumenResponse> siniestros
) {}
