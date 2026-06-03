package com.portfolio.siniestros.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración JPA separada de la clase principal para que @WebMvcTest
 * no intente cargar el contexto JPA cuando no hay base de datos.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
