package com.portfolio.siniestros.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.siniestros.dto.request.AsignarPeritoRequest;
import com.portfolio.siniestros.dto.request.CambioEstadoRequest;
import com.portfolio.siniestros.dto.request.SiniestroRequest;
import com.portfolio.siniestros.dto.response.PageResponse;
import com.portfolio.siniestros.dto.response.SiniestroResponse;
import com.portfolio.siniestros.entity.enums.EstadoSiniestro;
import com.portfolio.siniestros.exception.BusinessException;
import com.portfolio.siniestros.exception.ResourceNotFoundException;
import com.portfolio.siniestros.security.JwtService;
import com.portfolio.siniestros.security.UserDetailsServiceImpl;
import com.portfolio.siniestros.service.SiniestroService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.portfolio.siniestros.config.SecurityConfig;
import com.portfolio.siniestros.config.CorsConfig;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SiniestroController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("SiniestroController")
class SiniestroControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SiniestroService siniestroService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ── GET /api/siniestros ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "GESTOR")
    @DisplayName("GET /api/siniestros devuelve 200 con lista paginada")
    void listar_autenticado_devuelve200() throws Exception {
        var page = new PageResponse<SiniestroResponse>(List.of(), 0, 10, 0, 0, true);
        when(siniestroService.listar(any(), any(), eq(0), eq(10))).thenReturn(page);

        mvc.perform(get("/api/siniestros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/siniestros sin autenticación devuelve 401")
    void listar_sinAutenticacion_devuelve401() throws Exception {
        mvc.perform(get("/api/siniestros"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/siniestros/{id} ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PERITO")
    @DisplayName("GET /api/siniestros/1 devuelve 200 si existe")
    void obtener_existe_devuelve200() throws Exception {
        when(siniestroService.obtener(1L)).thenReturn(new SiniestroResponse(
                1L, "SIN-2026-0001", "desc", EstadoSiniestro.ABIERTO,
                null, null, null, null, null, null, null, null));

        mvc.perform(get("/api/siniestros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSiniestro").value("SIN-2026-0001"));
    }

    @Test
    @WithMockUser(roles = "GESTOR")
    @DisplayName("GET /api/siniestros/99 devuelve 404 si no existe")
    void obtener_noExiste_devuelve404() throws Exception {
        when(siniestroService.obtener(99L)).thenThrow(new ResourceNotFoundException("Siniestro", 99L));

        mvc.perform(get("/api/siniestros/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/siniestros ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "GESTOR")
    @DisplayName("POST /api/siniestros devuelve 201 con datos válidos")
    void crear_datosValidos_devuelve201() throws Exception {
        var request = new SiniestroRequest("Rotura tubería", new BigDecimal("3000"), 1L);
        var response = new SiniestroResponse(
                1L, "SIN-2026-0001", "Rotura tubería", EstadoSiniestro.ABIERTO,
                null, null, new BigDecimal("3000"), null, null, null, null, null);

        when(siniestroService.crear(any())).thenReturn(response);

        mvc.perform(post("/api/siniestros")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"));
    }

    @Test
    @WithMockUser(roles = "PERITO")
    @DisplayName("POST /api/siniestros con rol PERITO devuelve 403")
    void crear_rolPerito_devuelve403() throws Exception {
        mvc.perform(post("/api/siniestros")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SiniestroRequest("desc", null, 1L))))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/siniestros/{id}/estado ────────────────────────────────────

    @Test
    @WithMockUser(roles = "GESTOR")
    @DisplayName("PATCH /api/siniestros/1/estado devuelve 422 en violación de negocio")
    void cambiarEstado_violacionNegocio_devuelve422() throws Exception {
        when(siniestroService.cambiarEstado(eq(1L), any()))
                .thenThrow(new BusinessException("No se puede cambiar el estado de un siniestro RESUELTO"));

        mvc.perform(patch("/api/siniestros/1/estado")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambioEstadoRequest(EstadoSiniestro.ABIERTO, null, null))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── PATCH /api/siniestros/{id}/asignar-perito ─────────────────────────────

    @Test
    @WithMockUser(roles = "GESTOR")
    @DisplayName("PATCH /api/siniestros/1/asignar-perito devuelve 200 si es válido")
    void asignarPerito_valido_devuelve200() throws Exception {
        when(siniestroService.asignarPerito(eq(1L), any()))
                .thenReturn(mock(SiniestroResponse.class));

        mvc.perform(patch("/api/siniestros/1/asignar-perito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AsignarPeritoRequest(10L))))
                .andExpect(status().isOk());
    }

    private SiniestroResponse mock(Class<SiniestroResponse> clazz) {
        return new SiniestroResponse(1L, "SIN-2026-0001", "desc", EstadoSiniestro.ABIERTO,
                null, null, null, null, null, null, null, null);
    }
}
