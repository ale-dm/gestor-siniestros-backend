package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.ClienteRequest;
import com.portfolio.siniestros.dto.response.ClienteResponse;
import com.portfolio.siniestros.entity.Cliente;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.mapper.ClienteMapper;
import com.portfolio.siniestros.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService")
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @Mock ClienteMapper clienteMapper;

    @InjectMocks ClienteService service;

    private ClienteRequest requestValido() {
        return new ClienteRequest("Ana", "González", "12345678A",
                "ana@test.com", "600111222", "Calle Mayor 1");
    }

    private Cliente clienteBase() {
        return Cliente.builder()
                .id(1L).nombre("Ana").apellidos("González")
                .dni("12345678A").activo(true).build();
    }

    // ── crear() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("crea cliente cuando el DNI no existe")
        void crear_dniNuevo_ok() {
            var request = requestValido();
            var clienteGuardado = clienteBase();

            when(clienteRepository.existsByDni("12345678A")).thenReturn(false);
            when(clienteRepository.save(any())).thenReturn(clienteGuardado);
            when(clienteMapper.toResponse(clienteGuardado)).thenReturn(mock(ClienteResponse.class));

            service.crear(request);

            verify(clienteRepository).save(any(Cliente.class));
        }

        @Test
        @DisplayName("lanza BusinessException si el DNI ya existe")
        void crear_dniDuplicado_lanzaException() {
            when(clienteRepository.existsByDni("12345678A")).thenReturn(true);

            assertThatThrownBy(() -> service.crear(requestValido()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("12345678A");
        }
    }

    // ── actualizar() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("actualiza cliente cuando el DNI pertenece al mismo cliente")
        void actualizar_mismoDni_ok() {
            var cliente = clienteBase();

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByDniAndIdNot("12345678A", 1L)).thenReturn(false);
            when(clienteRepository.save(any())).thenReturn(cliente);
            when(clienteMapper.toResponse(any())).thenReturn(mock(ClienteResponse.class));

            service.actualizar(1L, requestValido());

            verify(clienteRepository).save(any());
        }

        @Test
        @DisplayName("lanza BusinessException si el DNI lo tiene otro cliente")
        void actualizar_dniDeOtroCliente_lanzaException() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBase()));
            when(clienteRepository.existsByDniAndIdNot("12345678A", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.actualizar(1L, requestValido()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("12345678A");
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si el cliente no existe")
        void actualizar_clienteNoExiste_lanzaException() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.actualizar(99L, requestValido()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── desactivar() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("desactivar()")
    class Desactivar {

        @Test
        @DisplayName("marca el cliente como inactivo (soft delete)")
        void desactivar_clienteExiste_setActivoFalse() {
            var cliente = clienteBase();
            assertThat(cliente.getActivo()).isTrue();

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any())).thenReturn(cliente);

            service.desactivar(1L);

            verify(clienteRepository).save(argThat(c -> !c.getActivo()));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si el cliente no existe")
        void desactivar_clienteNoExiste_lanzaException() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.desactivar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── obtener() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("obtener()")
    class Obtener {

        @Test
        @DisplayName("devuelve el cliente cuando existe")
        void obtener_existe_devuelveResponse() {
            var cliente = clienteBase();
            var response = mock(ClienteResponse.class);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteMapper.toResponse(cliente)).thenReturn(response);

            var result = service.obtener(1L);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si no existe")
        void obtener_noExiste_lanzaException() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtener(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
