package com.portfolio.siniestros.mapper;

import com.portfolio.siniestros.dto.response.PolizaResponse;
import com.portfolio.siniestros.dto.response.SiniestroResumenResponse;
import com.portfolio.siniestros.entity.Poliza;
import com.portfolio.siniestros.entity.Siniestro;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ClienteMapper.class})
public interface PolizaMapper {

    PolizaResponse toResponse(Poliza poliza);

    SiniestroResumenResponse toSiniestroResumen(Siniestro siniestro);
}
