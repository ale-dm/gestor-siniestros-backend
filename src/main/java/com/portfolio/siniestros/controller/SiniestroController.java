package com.portfolio.siniestros.controller;

import com.portfolio.siniestros.dto.request.AsignarPeritoRequest;
import com.portfolio.siniestros.dto.request.CambioEstadoRequest;
import com.portfolio.siniestros.dto.request.SiniestroRequest;
import com.portfolio.siniestros.dto.response.LogSiniestroResponse;
import com.portfolio.siniestros.dto.response.PageResponse;
import com.portfolio.siniestros.dto.response.SiniestroResponse;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import com.portfolio.siniestros.service.SiniestroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@RequiredArgsConstructor
public class SiniestroController {

    private final SiniestroService siniestroService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<PageResponse<SiniestroResponse>> listar(
            @RequestParam(required = false) EstadoSiniestro estado,
            @RequestParam(required = false) Long peritoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(siniestroService.listar(estado, peritoId, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<SiniestroResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(siniestroService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<SiniestroResponse> crear(@Valid @RequestBody SiniestroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siniestroService.crear(request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<SiniestroResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequest request
    ) {
        return ResponseEntity.ok(siniestroService.cambiarEstado(id, request));
    }

    @PatchMapping("/{id}/asignar-perito")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<SiniestroResponse> asignarPerito(
            @PathVariable Long id,
            @Valid @RequestBody AsignarPeritoRequest request
    ) {
        return ResponseEntity.ok(siniestroService.asignarPerito(id, request));
    }

    @GetMapping("/{id}/log")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<List<LogSiniestroResponse>> obtenerLog(@PathVariable Long id) {
        return ResponseEntity.ok(siniestroService.obtenerLog(id));
    }
}
