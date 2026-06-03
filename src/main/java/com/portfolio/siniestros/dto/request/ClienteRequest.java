package com.portfolio.siniestros.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 150) String apellidos,
        @NotBlank @Size(max = 20) String dni,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String telefono,
        @Size(max = 255) String direccion
) {}
