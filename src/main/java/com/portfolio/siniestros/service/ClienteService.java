package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.ClienteRequest;
import com.portfolio.siniestros.dto.response.ClienteResponse;
import com.portfolio.siniestros.dto.response.PageResponse;
import com.portfolio.siniestros.entity.Cliente;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.mapper.ClienteMapper;
import com.portfolio.siniestros.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public PageResponse<ClienteResponse> listar(String search, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("apellidos").ascending());
        return PageResponse.of(
                clienteRepository.findActivosBySearch(search, pageable)
                        .map(clienteMapper::toResponse)
        );
    }

    public ClienteResponse obtener(Long id) {
        return clienteMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDni(request.dni())) {
            throw new BusinessException("Ya existe un cliente con el DNI: " + request.dni());
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .dni(request.dni())
                .email(request.email())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .build();

        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = findOrThrow(id);

        if (clienteRepository.existsByDniAndIdNot(request.dni(), id)) {
            throw new BusinessException("Ya existe otro cliente con el DNI: " + request.dni());
        }

        cliente.setNombre(request.nombre());
        cliente.setApellidos(request.apellidos());
        cliente.setDni(request.dni());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());

        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = findOrThrow(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private Cliente findOrThrow(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
