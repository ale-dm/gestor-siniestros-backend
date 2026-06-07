package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.AsignarPeritoRequest;
import com.portfolio.siniestros.dto.request.CambioEstadoRequest;
import com.portfolio.siniestros.dto.request.SiniestroRequest;
import com.portfolio.siniestros.dto.response.LogSiniestroResponse;
import com.portfolio.siniestros.dto.response.PageResponse;
import com.portfolio.siniestros.dto.response.SiniestroResponse;
import com.portfolio.siniestros.entity.Poliza;
import com.portfolio.siniestros.entity.Siniestro;
import com.portfolio.siniestros.entity.Usuario;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import com.portfolio.siniestros.entity.enums.Rol;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.mapper.SiniestroMapper;
import com.portfolio.siniestros.repository.LogSiniestroRepository;
import com.portfolio.siniestros.repository.PolizaRepository;
import com.portfolio.siniestros.repository.SiniestroRepository;
import com.portfolio.siniestros.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiniestroService {

    private final SiniestroRepository siniestroRepository;
    private final PolizaRepository polizaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogSiniestroRepository logSiniestroRepository;
    private final LogSiniestroService logSiniestroService;
    private final SiniestroMapper siniestroMapper;
    private final TransaccionRetry transaccionRetry;

    public PageResponse<SiniestroResponse> listar(EstadoSiniestro estado, Long peritoId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("fechaApertura").descending());
        return PageResponse.of(
                siniestroRepository.findByFiltros(estado, peritoId, pageable)
                        .map(siniestroMapper::toResponse)
        );
    }

    public SiniestroResponse obtener(Long id) {
        return siniestroMapper.toResponse(findOrThrow(id));
    }

    public List<LogSiniestroResponse> obtenerLog(Long id) {
        findOrThrow(id);
        return logSiniestroRepository.findBySiniestroIdOrderByFechaAsc(id)
                .stream()
                .map(siniestroMapper::toLogResponse)
                .toList();
    }

    public SiniestroResponse crear(SiniestroRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        // El número se genera dentro de la transacción con reintento para evitar
        // colisiones del número secuencial bajo concurrencia.
        return transaccionRetry.enNuevaTransaccionConReintento(3, () -> {
            Poliza poliza = polizaRepository.findById(request.polizaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Póliza", request.polizaId()));

            Siniestro siniestro = Siniestro.builder()
                    .numeroSiniestro(generarNumero())
                    .descripcion(request.descripcion())
                    .estado(EstadoSiniestro.ABIERTO)
                    .fechaApertura(LocalDateTime.now())
                    .importeReclamado(request.importeReclamado())
                    .poliza(poliza)
                    .build();

            siniestro = siniestroRepository.save(siniestro);
            logSiniestroService.registrar(siniestro, null, "Siniestro abierto", usuarioActual);
            return siniestroMapper.toResponse(siniestro);
        });
    }

    @Transactional
    public SiniestroResponse cambiarEstado(Long id, CambioEstadoRequest request) {
        Siniestro siniestro = findOrThrow(id);
        EstadoSiniestro estadoAnterior = siniestro.getEstado();
        EstadoSiniestro estadoNuevo = request.estado();
        Usuario usuarioActual = getUsuarioActual();

        validarTransicion(siniestro, estadoNuevo, usuarioActual);

        if (estadoNuevo == EstadoSiniestro.RESUELTO) {
            if (request.importeIndemnizado() == null) {
                throw new BusinessException("El importe indemnizado es obligatorio para resolver el siniestro");
            }
            if (request.importeIndemnizado().compareTo(siniestro.getPoliza().getCoberturaMaxima()) > 0) {
                throw new BusinessException("El importe indemnizado supera la cobertura máxima de la póliza");
            }
            siniestro.setImporteIndemnizado(request.importeIndemnizado());
            siniestro.setFechaResolucion(LocalDateTime.now());
        }

        if (estadoNuevo == EstadoSiniestro.DENEGADO) {
            if (request.observaciones() == null || request.observaciones().isBlank()) {
                throw new BusinessException("Las observaciones son obligatorias para denegar el siniestro");
            }
            siniestro.setFechaResolucion(LocalDateTime.now());
        }

        siniestro.setEstado(estadoNuevo);
        if (request.observaciones() != null) {
            siniestro.setObservaciones(request.observaciones());
        }
        siniestro = siniestroRepository.save(siniestro);
        logSiniestroService.registrar(siniestro, estadoAnterior, request.observaciones(), usuarioActual);

        return siniestroMapper.toResponse(siniestro);
    }

    @Transactional
    public SiniestroResponse asignarPerito(Long id, AsignarPeritoRequest request) {
        Siniestro siniestro = findOrThrow(id);

        if (siniestro.getEstado() != EstadoSiniestro.ABIERTO) {
            throw new BusinessException("Solo se puede asignar perito a un siniestro en estado ABIERTO");
        }

        Usuario perito = usuarioRepository.findById(request.peritoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.peritoId()));

        if (perito.getRol() != Rol.PERITO) {
            throw new BusinessException("El usuario asignado debe tener rol PERITO");
        }

        if (Boolean.FALSE.equals(perito.getActivo())) {
            throw new BusinessException("No se puede asignar un perito desactivado");
        }

        siniestro.setPerito(perito);
        return siniestroMapper.toResponse(siniestroRepository.save(siniestro));
    }

    private void validarTransicion(Siniestro siniestro, EstadoSiniestro estadoNuevo, Usuario usuario) {
        EstadoSiniestro actual = siniestro.getEstado();

        if (actual == EstadoSiniestro.RESUELTO || actual == EstadoSiniestro.DENEGADO) {
            throw new BusinessException("No se puede cambiar el estado de un siniestro " + actual);
        }

        if (estadoNuevo == EstadoSiniestro.ABIERTO) {
            throw new BusinessException("No se puede revertir un siniestro al estado ABIERTO");
        }

        if (estadoNuevo == EstadoSiniestro.EN_PERITACION && siniestro.getPerito() == null) {
            throw new BusinessException("Debe asignar un perito antes de pasar a EN_PERITACION");
        }

        boolean esPerito = usuario != null && usuario.getRol() == Rol.PERITO;

        if ((estadoNuevo == EstadoSiniestro.RESUELTO || estadoNuevo == EstadoSiniestro.DENEGADO)) {
            if (actual != EstadoSiniestro.EN_PERITACION) {
                throw new BusinessException("Solo se puede resolver/denegar desde estado EN_PERITACION");
            }
            if (esPerito && !siniestro.getPerito().getId().equals(usuario.getId())) {
                throw new BusinessException("Solo el perito asignado puede resolver o denegar este siniestro");
            }
        }
    }

    private String generarNumero() {
        int anio = Year.now().getValue();
        String prefix = "SIN-" + anio + "-";
        int siguiente = siniestroRepository.findMaxSecuenciaByPrefix(prefix) + 1;
        return String.format("%s%04d", prefix, siguiente);
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return usuarioRepository.findByUsername(auth.getName()).orElse(null);
    }

    private Siniestro findOrThrow(Long id) {
        return siniestroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Siniestro", id));
    }
}
