package com.portfolio.siniestros.controller;

import com.portfolio.siniestros.dto.request.CambioEstadoPolizaRequest;
import com.portfolio.siniestros.dto.request.PolizaRequest;
import com.portfolio.siniestros.dto.response.PageResponse;
import com.portfolio.siniestros.dto.response.PolizaResponse;
import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import com.portfolio.siniestros.entity.enums.TipoPoliza;
import com.portfolio.siniestros.service.PolizaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/polizas")
@RequiredArgsConstructor
public class PolizaController {

    private final PolizaService polizaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<PageResponse<PolizaResponse>> listar(
            @RequestParam(required = false) EstadoPoliza estado,
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(polizaService.listar(estado, tipo, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<PolizaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<PolizaResponse> crear(@Valid @RequestBody PolizaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(polizaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<PolizaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PolizaRequest request
    ) {
        return ResponseEntity.ok(polizaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<PolizaResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoPolizaRequest request
    ) {
        return ResponseEntity.ok(polizaService.cambiarEstado(id, request));
    }
}
