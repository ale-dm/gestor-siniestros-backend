package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.response.*;
import com.portfolio.siniestros.mapper.ClienteMapper;
import com.portfolio.siniestros.mapper.SiniestroMapper;
import com.portfolio.siniestros.repository.ClienteRepository;
import com.portfolio.siniestros.repository.PolizaRepository;
import com.portfolio.siniestros.repository.SiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ClienteRepository clienteRepository;
    private final PolizaRepository polizaRepository;
    private final SiniestroRepository siniestroRepository;
    private final ClienteMapper clienteMapper;
    private final SiniestroMapper siniestroMapper;

    private static final int MAX_RESULTS = 5;

    public SearchResponse buscar(String q) {
        if (q == null || q.isBlank() || q.length() < 2) {
            return new SearchResponse(java.util.List.of(), java.util.List.of(), java.util.List.of());
        }

        var pageable = PageRequest.of(0, MAX_RESULTS);

        var clientes = clienteRepository.findActivosBySearch(q, pageable)
                .getContent().stream()
                .map(clienteMapper::toResumen)
                .toList();

        var polizas = polizaRepository.search(q, pageable).stream()
                .map(p -> new PolizaResumenResponse(
                        p.getId(), p.getNumeroPoliza(), p.getTipo(),
                        p.getEstado(), p.getFechaVencimiento(), p.getCoberturaMaxima()))
                .toList();

        var siniestros = siniestroRepository.search(q, pageable).stream()
                .map(siniestroMapper::toSiniestroResumen)
                .toList();

        return new SearchResponse(clientes, polizas, siniestros);
    }
}
