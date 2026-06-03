package com.portfolio.siniestros.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ClienteResponse(
        Long id,
        String nombre,
        String apellidos,
        String dni,
        String email,
        String telefono,
        String direccion,
        Boolean activo,
        List<PolizaResumenResponse> polizasActivas,
        LocalDateTime createdAt
) {}
