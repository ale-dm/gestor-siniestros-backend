package com.portfolio.siniestros.controller;

import com.portfolio.siniestros.dto.response.UsuarioResumenResponse;
import com.portfolio.siniestros.entity.enums.Rol;
import com.portfolio.siniestros.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResumenResponse>> listar() {
        return ResponseEntity.ok(
                usuarioRepository.findByActivoTrue().stream()
                        .map(u -> new UsuarioResumenResponse(u.getId(), u.getUsername(), u.getNombre(), u.getApellidos(), u.getRol()))
                        .toList()
        );
    }

    @GetMapping("/peritos")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<List<UsuarioResumenResponse>> peritos() {
        return ResponseEntity.ok(
                usuarioRepository.findByRolAndActivoTrue(Rol.PERITO).stream()
                        .map(u -> new UsuarioResumenResponse(u.getId(), u.getUsername(), u.getNombre(), u.getApellidos(), u.getRol()))
                        .toList()
        );
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(true);
            usuarioRepository.save(u);
        });
        return ResponseEntity.noContent().build();
    }
}
