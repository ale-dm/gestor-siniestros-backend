package com.portfolio.siniestros.repository;

import com.portfolio.siniestros.entity.Siniestro;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {

    @Query("""
            SELECT s FROM Siniestro s
            WHERE (:estado IS NULL OR s.estado = :estado)
            AND (:peritoId IS NULL OR s.perito.id = :peritoId)
            """)
    Page<Siniestro> findByFiltros(
            @Param("estado") EstadoSiniestro estado,
            @Param("peritoId") Long peritoId,
            Pageable pageable
    );

    @Query("SELECT COUNT(s) FROM Siniestro s WHERE s.estado = :estado")
    long countByEstado(@Param("estado") EstadoSiniestro estado);

    @Query("SELECT COALESCE(SUM(s.importeIndemnizado), 0) FROM Siniestro s WHERE s.estado = 'RESUELTO'")
    BigDecimal sumImporteIndemnizado();

    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (fecha_resolucion - fecha_apertura)) / 86400)
            FROM siniestros
            WHERE estado IN ('RESUELTO', 'DENEGADO')
            AND fecha_resolucion IS NOT NULL
            """, nativeQuery = true)
    Double avgDiasResolucion();

    @Query("""
            SELECT s.poliza.tipo, COUNT(s)
            FROM Siniestro s
            GROUP BY s.poliza.tipo
            """)
    List<Object[]> countByTipoPoliza();

    List<Siniestro> findTop5ByEstadoOrderByFechaAperturaDesc(EstadoSiniestro estado);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.numeroSiniestro, 10) AS int)), 0) FROM Siniestro s WHERE s.numeroSiniestro LIKE :prefix%")
    int findMaxSecuenciaByPrefix(@Param("prefix") String prefix);
}
