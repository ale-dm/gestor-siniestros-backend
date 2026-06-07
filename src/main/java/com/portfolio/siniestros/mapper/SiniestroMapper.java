package com.portfolio.siniestros.mapper;

import com.portfolio.siniestros.dto.response.LogSiniestroResponse;
import com.portfolio.siniestros.dto.response.SiniestroResumenResponse;
import com.portfolio.siniestros.dto.response.SiniestroResponse;
import com.portfolio.siniestros.dto.response.UsuarioResumenResponse;
import com.portfolio.siniestros.entity.LogSiniestro;
import com.portfolio.siniestros.entity.Siniestro;
import com.portfolio.siniestros.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {PolizaMapper.class}, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SiniestroMapper {

    SiniestroResponse toResponse(Siniestro siniestro);

    SiniestroResumenResponse toSiniestroResumen(Siniestro siniestro);

    LogSiniestroResponse toLogResponse(LogSiniestro log);

    UsuarioResumenResponse toUsuarioResumen(Usuario usuario);
}
