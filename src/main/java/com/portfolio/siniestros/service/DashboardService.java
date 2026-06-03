package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.response.DashboardResponse;
import com.portfolio.siniestros.dto.response.SiniestroResumenResponse;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import com.portfolio.siniestros.entity.enums.TipoPoliza;
import com.portfolio.siniestros.mapper.SiniestroMapper;
import com.portfolio.siniestros.repository.SiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SiniestroRepository siniestroRepository;
    private final SiniestroMapper siniestroMapper;

    public DashboardResponse resumen() {
        Map<String, Long> distribucion = Arrays.stream(TipoPoliza.values())
                .collect(Collectors.toMap(Enum::name, t -> 0L));

        siniestroRepository.countByTipoPoliza()
                .forEach(row -> distribucion.put(row[0].toString(), (Long) row[1]));

        List<SiniestroResumenResponse> ultimosAbiertos = siniestroRepository
                .findTop5ByEstadoOrderByFechaAperturaDesc(EstadoSiniestro.ABIERTO)
                .stream()
                .map(siniestroMapper::toSiniestroResumen)
                .toList();

        BigDecimal totalIndemnizado = siniestroRepository.sumImporteIndemnizado();
        Double mediaDias = siniestroRepository.avgDiasResolucion();

        return new DashboardResponse(
                siniestroRepository.countByEstado(EstadoSiniestro.ABIERTO),
                siniestroRepository.countByEstado(EstadoSiniestro.EN_PERITACION),
                siniestroRepository.countByEstado(EstadoSiniestro.RESUELTO),
                siniestroRepository.countByEstado(EstadoSiniestro.DENEGADO),
                totalIndemnizado != null ? totalIndemnizado : BigDecimal.ZERO,
                mediaDias,
                distribucion,
                ultimosAbiertos
        );
    }
}
