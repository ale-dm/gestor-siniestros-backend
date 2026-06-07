package com.portfolio.siniestros.service;

import com.portfolio.siniestros.dto.request.AsignarPeritoRequest;
import com.portfolio.siniestros.dto.request.CambioEstadoRequest;
import com.portfolio.siniestros.dto.request.SiniestroRequest;
import com.portfolio.siniestros.dto.response.SiniestroResponse;
import com.portfolio.siniestros.entity.Poliza;
import com.portfolio.siniestros.entity.Siniestro;
import com.portfolio.siniestros.entity.Usuario;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import com.portfolio.siniestros.entity.enums.Rol;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.mapper.SiniestroMapper;
import com.portfolio.siniestros.repository.LogSiniestroRepository;
import com.portfolio.siniestros.repository.PolizaRepository;
import com.portfolio.siniestros.repository.SiniestroRepository;
import com.portfolio.siniestros.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiniestroService")
class SiniestroServiceTest {

    @Mock SiniestroRepository siniestroRepository;
    @Mock PolizaRepository polizaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock LogSiniestroRepository logSiniestroRepository;
    @Mock LogSiniestroService logSiniestroService;
    @Mock SiniestroMapper siniestroMapper;
    @Mock TransaccionRetry transaccionRetry;

    @InjectMocks SiniestroService service;

    // ── Fixtures ────────────────────────────────────────────────────────────

    private Poliza polizaBase() {
        return Poliza.builder()
                .id(1L)
                .numeroPoliza("POL-2026-00001")
                .coberturaMaxima(new BigDecimal("50000"))
                .build();
    }

    private Siniestro siniestroAbierto() {
        return Siniestro.builder()
                .id(1L)
                .numeroSiniestro("SIN-2026-0001")
                .estado(EstadoSiniestro.ABIERTO)
                .fechaApertura(LocalDateTime.now())
                .poliza(polizaBase())
                .build();
    }

    private Siniestro siniestroEnPeritacion(Usuario perito) {
        Siniestro s = siniestroAbierto();
        s.setEstado(EstadoSiniestro.EN_PERITACION);
        s.setPerito(perito);
        return s;
    }

    private Usuario perito() {
        return Usuario.builder().id(10L).username("perito1").rol(Rol.PERITO).activo(true).build();
    }

    private Usuario gestor() {
        return Usuario.builder().id(20L).username("gestor1").rol(Rol.GESTOR).activo(true).build();
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        lenient().when(usuarioRepository.findByUsername(usuario.getUsername()))
                .thenReturn(Optional.of(usuario));
    }

    @BeforeEach
    void limpiarSecurityContext() {
        SecurityContextHolder.clearContext();
        // El helper de reintento ejecuta la acción directamente en los tests unitarios.
        lenient().when(transaccionRetry.enNuevaTransaccionConReintento(anyInt(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
    }

    // ── crear() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("crea siniestro en estado ABIERTO cuando la póliza existe")
        void crear_polizaExiste_creaEnEstadoAbierto() {
            var request = new SiniestroRequest("Rotura tubería", new BigDecimal("3000"), 1L);
            var poliza = polizaBase();
            var siniestroGuardado = siniestroAbierto();

            when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
            when(siniestroRepository.findMaxSecuenciaByPrefix(anyString())).thenReturn(0);
            when(siniestroRepository.save(any())).thenReturn(siniestroGuardado);
            when(siniestroMapper.toResponse(siniestroGuardado)).thenReturn(mock(SiniestroResponse.class));

            service.crear(request);

            verify(siniestroRepository).save(argThat(s ->
                    s.getEstado() == EstadoSiniestro.ABIERTO &&
                    s.getDescripcion().equals("Rotura tubería")
            ));
            verify(logSiniestroService).registrar(any(), isNull(), anyString(), any());
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si la póliza no existe")
        void crear_polizaNoExiste_lanzaException() {
            when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.crear(new SiniestroRequest("desc", null, 99L)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── cambiarEstado() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarEstado()")
    class CambiarEstado {

        @Test
        @DisplayName("ABIERTO → EN_PERITACION es válido cuando hay perito asignado")
        void abierto_a_enPeritacion_conPerito_ok() {
            var perito = perito();
            var siniestro = siniestroAbierto();
            siniestro.setPerito(perito);

            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));
            when(siniestroRepository.save(any())).thenReturn(siniestro);
            when(siniestroMapper.toResponse(any())).thenReturn(mock(SiniestroResponse.class));

            service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.EN_PERITACION, null, null));

            verify(siniestroRepository).save(argThat(s -> s.getEstado() == EstadoSiniestro.EN_PERITACION));
        }

        @Test
        @DisplayName("ABIERTO → EN_PERITACION sin perito lanza BusinessException")
        void abierto_a_enPeritacion_sinPerito_lanzaException() {
            var siniestro = siniestroAbierto(); // sin perito
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.EN_PERITACION, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("perito");
        }

        @Test
        @DisplayName("EN_PERITACION → RESUELTO con importe válido es correcto")
        void enPeritacion_a_resuelto_importeValido_ok() {
            var perito = perito();
            var siniestro = siniestroEnPeritacion(perito);

            autenticarComo(perito);
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));
            when(siniestroRepository.save(any())).thenReturn(siniestro);
            when(siniestroMapper.toResponse(any())).thenReturn(mock(SiniestroResponse.class));

            service.cambiarEstado(1L,
                    new CambioEstadoRequest(EstadoSiniestro.RESUELTO, new BigDecimal("10000"), null));

            verify(siniestroRepository).save(argThat(s ->
                    s.getEstado() == EstadoSiniestro.RESUELTO &&
                    s.getFechaResolucion() != null
            ));
        }

        @Test
        @DisplayName("EN_PERITACION → RESUELTO sin importe lanza BusinessException")
        void enPeritacion_a_resuelto_sinImporte_lanzaException() {
            var siniestro = siniestroEnPeritacion(perito());
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.RESUELTO, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("importe indemnizado");
        }

        @Test
        @DisplayName("RESUELTO → DENEGADO: importe supera cobertura lanza BusinessException")
        void resolucion_importeSuperaCobertura_lanzaException() {
            var siniestro = siniestroEnPeritacion(perito());
            // cobertura máxima es 50000
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L,
                            new CambioEstadoRequest(EstadoSiniestro.RESUELTO, new BigDecimal("99999"), null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cobertura máxima");
        }

        @Test
        @DisplayName("EN_PERITACION → DENEGADO sin observaciones lanza BusinessException")
        void enPeritacion_a_denegado_sinObservaciones_lanzaException() {
            var siniestro = siniestroEnPeritacion(perito());
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.DENEGADO, null, "")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("observaciones");
        }

        @Test
        @DisplayName("No se puede cambiar el estado de un siniestro RESUELTO")
        void resuelto_noPuedeCambiarEstado() {
            var siniestro = siniestroAbierto();
            siniestro.setEstado(EstadoSiniestro.RESUELTO);
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.ABIERTO, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RESUELTO");
        }

        @Test
        @DisplayName("No se puede cambiar el estado de un siniestro DENEGADO")
        void denegado_noPuedeCambiarEstado() {
            var siniestro = siniestroAbierto();
            siniestro.setEstado(EstadoSiniestro.DENEGADO);
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.ABIERTO, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("DENEGADO");
        }

        @Test
        @DisplayName("Perito no asignado no puede resolver el siniestro")
        void peritoNoAsignado_noPuedeResolver() {
            var peritoAsignado = perito();
            var otroPerito = Usuario.builder().id(99L).username("otro").rol(Rol.PERITO).activo(true).build();
            var siniestro = siniestroEnPeritacion(peritoAsignado);

            autenticarComo(otroPerito);
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L,
                            new CambioEstadoRequest(EstadoSiniestro.RESUELTO, new BigDecimal("1000"), null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("perito asignado");
        }

        @Test
        @DisplayName("No se puede revertir un siniestro al estado ABIERTO")
        void revertir_a_abierto_lanzaException() {
            var siniestro = siniestroEnPeritacion(perito());
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L, new CambioEstadoRequest(EstadoSiniestro.ABIERTO, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ABIERTO");
        }

        @Test
        @DisplayName("No se puede resolver directamente desde ABIERTO")
        void abierto_a_resuelto_directo_lanzaException() {
            var siniestro = siniestroAbierto();
            siniestro.setPerito(perito());
            autenticarComo(gestor());
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() ->
                    service.cambiarEstado(1L,
                            new CambioEstadoRequest(EstadoSiniestro.RESUELTO, new BigDecimal("1000"), null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("EN_PERITACION");
        }
    }

    // ── asignarPerito() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("asignarPerito()")
    class AsignarPerito {

        @Test
        @DisplayName("asigna perito correctamente a un siniestro ABIERTO")
        void asignar_siniestroAbierto_ok() {
            var perito = perito();
            var siniestro = siniestroAbierto();

            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(perito));
            when(siniestroRepository.save(any())).thenReturn(siniestro);
            when(siniestroMapper.toResponse(any())).thenReturn(mock(SiniestroResponse.class));

            service.asignarPerito(1L, new AsignarPeritoRequest(10L));

            verify(siniestroRepository).save(argThat(s -> s.getPerito() != null && s.getPerito().getId() == 10L));
        }

        @Test
        @DisplayName("lanza BusinessException si el siniestro no está ABIERTO")
        void asignar_siniestroNoAbierto_lanzaException() {
            var siniestro = siniestroAbierto();
            siniestro.setEstado(EstadoSiniestro.EN_PERITACION);

            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));

            assertThatThrownBy(() -> service.asignarPerito(1L, new AsignarPeritoRequest(10L)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ABIERTO");
        }

        @Test
        @DisplayName("lanza BusinessException si el usuario no tiene rol PERITO")
        void asignar_usuarioNoEsPerito_lanzaException() {
            var gestor = gestor();
            var siniestro = siniestroAbierto();

            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro));
            when(usuarioRepository.findById(20L)).thenReturn(Optional.of(gestor));

            assertThatThrownBy(() -> service.asignarPerito(1L, new AsignarPeritoRequest(20L)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("PERITO");
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si el usuario no existe")
        void asignar_usuarioNoExiste_lanzaException() {
            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestroAbierto()));
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.asignarPerito(1L, new AsignarPeritoRequest(99L)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("lanza BusinessException si el perito está desactivado")
        void asignar_peritoDesactivado_lanzaException() {
            var peritoInactivo = Usuario.builder().id(10L).username("perito1").rol(Rol.PERITO).activo(false).build();

            when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestroAbierto()));
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(peritoInactivo));

            assertThatThrownBy(() -> service.asignarPerito(1L, new AsignarPeritoRequest(10L)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("desactivado");
        }
    }
}
