package com.portfolio.siniestros.mapper;

import com.portfolio.siniestros.dto.response.ClienteResumenResponse;
import com.portfolio.siniestros.dto.response.ClienteResponse;
import com.portfolio.siniestros.dto.response.PolizaResumenResponse;
import com.portfolio.siniestros.entity.Cliente;
import com.portfolio.siniestros.entity.Poliza;
import com.portfolio.siniestros.entity.enums.EstadoPoliza;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClienteMapper {

    ClienteResumenResponse toResumen(Cliente cliente);

    @Mapping(target = "polizasActivas", expression = "java(mapPolizasActivas(cliente))")
    ClienteResponse toResponse(Cliente cliente);

    default List<PolizaResumenResponse> mapPolizasActivas(Cliente cliente) {
        return cliente.getPolizas().stream()
                .filter(p -> p.getEstado() == EstadoPoliza.ACTIVA)
                .map(this::toPolizaResumen)
                .toList();
    }

    PolizaResumenResponse toPolizaResumen(Poliza poliza);
}
