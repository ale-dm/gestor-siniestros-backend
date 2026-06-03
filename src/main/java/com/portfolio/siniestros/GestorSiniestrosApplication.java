package com.portfolio.siniestros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GestorSiniestrosApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestorSiniestrosApplication.class, args);
    }
}
