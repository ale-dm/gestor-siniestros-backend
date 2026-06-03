package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.Rol;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String username,
        String nombre,
        Rol rol
) {}
