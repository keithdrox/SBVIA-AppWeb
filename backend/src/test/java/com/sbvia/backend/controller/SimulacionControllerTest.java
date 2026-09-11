package com.sbvia.backend.controller;

import com.sbvia.backend.dto.EstadisticasDTO;
import com.sbvia.backend.dto.FinalizarSimulacionRequest;
import com.sbvia.backend.dto.MetricasConduccionRequest;
import com.sbvia.backend.dto.ResultadoConduccionDTO;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.service.RetroalimentacionService;
import com.sbvia.backend.service.SimulacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimulacionControllerTest {

    @Mock
    private SimulacionService simulacionService;

    @Mock
    private RetroalimentacionService retroalimentacionService;

    @InjectMocks
    private SimulacionController controller;

    @Test
    void testIniciar() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        when(simulacionService.iniciarSimulacion("user", 1)).thenReturn(new SimulacionDTO());

        ResponseEntity<SimulacionDTO> res = controller.iniciar(1, auth);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testFinalizar() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        when(simulacionService.finalizarSimulacion(eq("user"), eq(1), any(BigDecimal.class))).thenReturn(new SimulacionDTO());

        ResponseEntity<SimulacionDTO> res = controller.finalizar(1, new FinalizarSimulacionRequest(BigDecimal.valueOf(100)), auth);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testFinalizarConduccion() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        when(simulacionService.finalizarConduccion(eq("user"), eq(1), any(MetricasConduccionRequest.class))).thenReturn(new ResultadoConduccionDTO());

        MetricasConduccionRequest request = new MetricasConduccionRequest(
            0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0, 0, 0, 0
        );
        
        ResponseEntity<ResultadoConduccionDTO> res = controller.finalizarConduccion(1, request, auth);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testRetroalimentacion() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        when(retroalimentacionService.generarInforme("user", 1)).thenReturn(new RetroalimentacionIaResponse());

        ResponseEntity<RetroalimentacionIaResponse> res = controller.retroalimentacion(1, auth);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testObtenerMisPracticas() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        when(simulacionService.obtenerMisPracticas("user")).thenReturn(List.of(new SimulacionDTO()));

        ResponseEntity<List<SimulacionDTO>> res = controller.obtenerMisPracticas(auth);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testObtenerTodas() {
        when(simulacionService.obtenerTodas()).thenReturn(List.of(new SimulacionDTO()));
        ResponseEntity<List<SimulacionDTO>> res = controller.obtenerTodas();
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testObtenerEstadisticasGlobales() {
        when(simulacionService.obtenerEstadisticasGlobales()).thenReturn(new EstadisticasDTO(1, 100, 50));
        ResponseEntity<EstadisticasDTO> res = controller.obtenerEstadisticasGlobales();
        assertEquals(200, res.getStatusCode().value());
    }
}
