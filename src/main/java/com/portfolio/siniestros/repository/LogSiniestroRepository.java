package com.portfolio.siniestros.repository;

import com.portfolio.siniestros.entity.LogSiniestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogSiniestroRepository extends JpaRepository<LogSiniestro, Long> {

    List<LogSiniestro> findBySiniestroIdOrderByFechaAsc(Long siniestroId);
}
