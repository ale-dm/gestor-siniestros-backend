package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.CambioEstadoPolizaRequest;
import com.portfolio.siniestros.dto.request.PolizaRequest;
import com.portfolio.siniestros.dto.response.PageResponse;
import com.portfolio.siniestros.dto.response.PolizaResponse;
import com.portfolio.siniestros.entity.Cliente;
import com.portfolio.siniestros.entity.Poliza;
import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import com.portfolio.siniestros.entity.enums.TipoPoliza;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.mapper.PolizaMapper;
import com.portfolio.siniestros.repository.ClienteRepository;
import com.portfolio.siniestros.repository.PolizaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final ClienteRepository clienteRepository;
    private final PolizaMapper polizaMapper;

    public PageResponse<PolizaResponse> listar(EstadoPoliza estado, TipoPoliza tipo, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(
                polizaRepository.findByFiltros(estado, tipo, pageable)
                        .map(polizaMapper::toResponse)
        );
    }

    public PolizaResponse obtener(Long id) {
        return polizaMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public PolizaResponse crear(PolizaRequest request) {
        validarFechas(request);

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.clienteId()));

        Poliza poliza = Poliza.builder()
                .numeroPoliza(generarNumero())
                .tipo(request.tipo())
                .estado(EstadoPoliza.ACTIVA)
                .fechaInicio(request.fechaInicio())
                .fechaVencimiento(request.fechaVencimiento())
                .coberturaMaxima(request.coberturaMaxima())
                .primaMensual(request.primaMensual())
                .descripcion(request.descripcion())
                .cliente(cliente)
                .build();

        return polizaMapper.toResponse(polizaRepository.save(poliza));
    }

    @Transactional
    public PolizaResponse actualizar(Long id, PolizaRequest request) {
        Poliza poliza = findOrThrow(id);
        validarFechas(request);

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.clienteId()));

        poliza.setTipo(request.tipo());
        poliza.setFechaInicio(request.fechaInicio());
        poliza.setFechaVencimiento(request.fechaVencimiento());
        poliza.setCoberturaMaxima(request.coberturaMaxima());
        poliza.setPrimaMensual(request.primaMensual());
        poliza.setDescripcion(request.descripcion());
        poliza.setCliente(cliente);

        return polizaMapper.toResponse(polizaRepository.save(poliza));
    }

    @Transactional
    public PolizaResponse cambiarEstado(Long id, CambioEstadoPolizaRequest request) {
        Poliza poliza = findOrThrow(id);
        poliza.setEstado(request.estado());
        return polizaMapper.toResponse(polizaRepository.save(poliza));
    }

    private void validarFechas(PolizaRequest request) {
        if (!request.fechaVencimiento().isAfter(request.fechaInicio())) {
            throw new BusinessException("La fecha de vencimiento debe ser posterior a la fecha de inicio");
        }
    }

    private String generarNumero() {
        int anio = Year.now().getValue();
        String prefix = "POL-" + anio + "-";
        int siguiente = polizaRepository.findMaxSecuenciaByPrefix(prefix) + 1;
        return String.format("%s%05d", prefix, siguiente);
    }

    private Poliza findOrThrow(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza", id));
    }
}
