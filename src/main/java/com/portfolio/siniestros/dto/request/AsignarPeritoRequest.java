package com.portfolio.siniestros.dto.request;

import jakarta.validation.constraints.NotNull;

public record AsignarPeritoRequest(
        @NotNull Long peritoId
) {}
