package com.portfolio.siniestros.dto.response;

import com.portfolio.siniestros.entity.enums.EstadoSiniestro;

import java.time.LocalDateTime;

public record LogSiniestroResponse(
        Long id,
        EstadoSiniestro estadoAnterior,
        EstadoSiniestro estadoNuevo,
        String observaciones,
        UsuarioResumenResponse usuario,
        LocalDateTime fecha
) {}
