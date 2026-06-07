package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.LoginRequest;
import com.portfolio.siniestros.dto.request.RefreshTokenRequest;
import com.portfolio.siniestros.dto.response.AuthResponse;
import com.portfolio.siniestros.entity.Usuario;
import com.portfolio.siniestros.repository.UsuarioRepository;
import com.portfolio.siniestros.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        Usuario usuario = usuarioRepository.findByUsername(request.username()).orElseThrow();

        return new AuthResponse(
                jwtService.generateToken(userDetails),
                jwtService.generateRefreshToken(userDetails),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol()
        );
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        final String refreshToken = request.refreshToken();

        final String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isRefreshToken(refreshToken) || !jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        return new AuthResponse(
                jwtService.generateToken(userDetails),
                request.refreshToken(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol()
        );
    }
}
