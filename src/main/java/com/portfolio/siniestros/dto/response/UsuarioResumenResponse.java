package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.Rol;

public record UsuarioResumenResponse(
        Long id,
        String username,
        String nombre,
        String apellidos,
        Rol rol
) {}
