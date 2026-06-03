package com.portfolio.siniestros.service;

import com.portfolio.siniestros.entity.LogSiniestro;
import com.portfolio.siniestros.entity.Siniestro;
import com.portfolio.siniestros.entity.Usuario;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import com.portfolio.siniestros.repository.LogSiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogSiniestroService {

    private final LogSiniestroRepository logSiniestroRepository;

    public void registrar(Siniestro siniestro, EstadoSiniestro estadoAnterior, String observaciones, Usuario usuario) {
        LogSiniestro log = LogSiniestro.builder()
                .siniestro(siniestro)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(siniestro.getEstado())
                .observaciones(observaciones)
                .usuario(usuario)
                .build();
        logSiniestroRepository.save(log);
    }
}
