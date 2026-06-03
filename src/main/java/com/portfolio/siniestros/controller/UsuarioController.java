package com.portfolio.siniestros.controller;

import com.portfolio.siniestros.dto.request.UsuarioRequest;
import com.portfolio.siniestros.dto.response.UsuarioResumenResponse;
import com.portfolio.siniestros.dto.response.UsuarioResponse;
import com.portfolio.siniestros.entity.enums.Rol;
import com.portfolio.siniestros.repository.UsuarioRepository;
import com.portfolio.siniestros.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/peritos")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<List<UsuarioResumenResponse>> peritos() {
        return ResponseEntity.ok(
                usuarioRepository.findByRolAndActivoTrue(Rol.PERITO).stream()
                        .map(u -> new UsuarioResumenResponse(u.getId(), u.getUsername(),
                                u.getNombre(), u.getApellidos(), u.getRol()))
                        .toList()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        usuarioService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
