package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.Rol;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String apellidos,
        String email,
        Rol rol,
        Boolean activo,
        LocalDateTime createdAt
) {}
