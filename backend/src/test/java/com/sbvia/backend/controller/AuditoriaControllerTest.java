package com.sbvia.backend.controller;

import com.sbvia.backend.entity.BitacoraAuditoria;
import com.sbvia.backend.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditoriaControllerTest {

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AuditoriaController controller;

    @Test
    void testObtenerAuditoria() {
        when(auditoriaService.obtenerAuditoria(any(), any(), any(), any(), any())).thenReturn(List.of(new BitacoraAuditoria()));
        ResponseEntity<List<BitacoraAuditoria>> res = controller.obtenerAuditoria(null, null, null, null, null);
        assertEquals(200, res.getStatusCode().value());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    void testDescargarReportePdf() {
        when(auditoriaService.generarReportePdf(any(), any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});
        ResponseEntity<byte[]> res = controller.descargarReportePdf(null, null, null, null, null);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("application/pdf", res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertNotNull(res.getBody());
    }
}
