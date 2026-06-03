package com.portfolio.siniestros.repository;

import com.portfolio.siniestros.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    @Query("""
            SELECT c FROM Cliente c
            WHERE c.activo = true
            AND (:search IS NULL OR :search = ''
                 OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.dni) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Cliente> findActivosBySearch(@Param("search") String search, Pageable pageable);
}
