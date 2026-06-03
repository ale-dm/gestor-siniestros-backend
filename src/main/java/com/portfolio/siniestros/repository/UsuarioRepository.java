package com.portfolio.siniestros.repository;

import com.portfolio.siniestros.entity.Usuario;
import com.portfolio.siniestros.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Usuario> findByRolAndActivoTrue(Rol rol);

    List<Usuario> findByActivoTrue();
}
