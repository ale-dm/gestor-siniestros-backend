package com.portfolio.siniestros.dto.response;

public record ClienteResumenResponse(
        Long id,
        String nombre,
        String apellidos,
        String dni
) {}
