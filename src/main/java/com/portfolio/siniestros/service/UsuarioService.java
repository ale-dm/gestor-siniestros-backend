package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.UsuarioRequest;
import com.portfolio.siniestros.dto.response.UsuarioResponse;
import com.portfolio.siniestros.entity.Usuario;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BusinessException("Ya existe un usuario con el username: " + request.username());
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email: " + request.email());
        }

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .email(request.email())
                .rol(request.rol())
                .activo(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void activar(Long id) {
        Usuario u = findOrThrow(id);
        u.setActivo(true);
        usuarioRepository.save(u);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario u = findOrThrow(id);
        u.setActivo(false);
        usuarioRepository.save(u);
    }

    private Usuario findOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getUsername(), u.getNombre(), u.getApellidos(),
                u.getEmail(), u.getRol(), u.getActivo(), u.getCreatedAt()
        );
    }
}
