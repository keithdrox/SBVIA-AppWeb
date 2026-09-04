package com.sbvia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbvia.backend.dto.MetricasConduccionRequest;
import com.sbvia.backend.dto.ResultadoConduccionDTO;
import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.security.JwtService;
import com.sbvia.backend.service.RetroalimentacionService;
import com.sbvia.backend.service.SimulacionService;
import com.sbvia.backend.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de POST /api/simulaciones/{id}/conduccion/finalizar.
 * Verifica ruta, validación Bean Validation y autenticación.
 * Nota: usa la cadena de seguridad por defecto del slice de test; la emisión
 * de la cookie XSRF de la SecurityConfig real se revisa en la Etapa 5.
 */
@WebMvcTest(SimulacionController.class)
class SimulacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SimulacionService simulacionService;

    @MockBean
    private RetroalimentacionService retroalimentacionService;

    @MockBean
    private CsrfTokenRepository csrfTokenRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    private String cuerpoValido() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "duracionSegundos", 120,
                "velocidadPromedio", new BigDecimal("45.50"),
                "velocidadMaxima", new BigDecimal("72.00"),
                "excesosVelocidad", 2,
                "colisiones", 1,
                "salidasCarril", 1,
                "semaforosIgnorados", 1,
                "distanciaInsegura", 1));
    }

    @Test
    @WithMockUser
    void finalizaConduccionDevuelveElPuntajeDelServidor() throws Exception {
        when(simulacionService.finalizarConduccion(eq("user"), eq(21), any(MetricasConduccionRequest.class)))
                .thenReturn(ResultadoConduccionDTO.builder()
                        .simulacion(SimulacionDTO.builder()
                                .idSimulacion(21)
                                .puntajeFinal(new BigDecimal("12.00"))
                                .build())
                        .build());

        mockMvc.perform(post("/api/simulaciones/21/conduccion/finalizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulacion.idSimulacion").value(21))
                .andExpect(content().string(containsString("\"puntajeFinal\":12.00")));

        verify(simulacionService).finalizarConduccion(eq("user"), eq(21), any(MetricasConduccionRequest.class));
    }

    @Test
    @WithMockUser
    void rechazaMetricasInvalidas() throws Exception {
        String invalido = objectMapper.writeValueAsString(Map.of(
                "duracionSegundos", 0,
                "velocidadPromedio", new BigDecimal("-1"),
                "velocidadMaxima", new BigDecimal("500"),
                "excesosVelocidad", -2,
                "colisiones", 0,
                "salidasCarril", 0,
                "semaforosIgnorados", 0,
                "distanciaInsegura", 0));

        mockMvc.perform(post("/api/simulaciones/21/conduccion/finalizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exigeAutenticacion() throws Exception {
        mockMvc.perform(post("/api/simulaciones/21/conduccion/finalizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoValido()))
                .andExpect(status().isUnauthorized());
    }
}
