package com.portfolio.siniestros.repository;

import com.portfolio.siniestros.entity.Poliza;
import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import com.portfolio.siniestros.entity.enums.TipoPoliza;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    boolean existsByNumeroPoliza(String numeroPoliza);

    @Query("""
            SELECT p FROM Poliza p
            WHERE (:estado IS NULL OR p.estado = :estado)
            AND (:tipo IS NULL OR p.tipo = :tipo)
            """)
    Page<Poliza> findByFiltros(
            @Param("estado") EstadoPoliza estado,
            @Param("tipo") TipoPoliza tipo,
            Pageable pageable
    );

    List<Poliza> findByClienteIdAndEstado(Long clienteId, EstadoPoliza estado);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.numeroPoliza, 10) AS int)), 0) FROM Poliza p WHERE p.numeroPoliza LIKE :prefix%")
    int findMaxSecuenciaByPrefix(@Param("prefix") String prefix);

    @Query("""
            SELECT p FROM Poliza p
            WHERE LOWER(p.numeroPoliza) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<Poliza> search(@Param("q") String q, Pageable pageable);
}
