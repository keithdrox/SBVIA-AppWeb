package com.sbvia.backend.service;

import com.lowagie.text.pdf.PdfReader;
import com.sbvia.backend.entity.BitacoraAuditoria;
import com.sbvia.backend.repository.BitacoraAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditoriaServiceTest {

    @Mock
    private BitacoraAuditoriaRepository repository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private BitacoraAuditoria log1;
    private BitacoraAuditoria log2;

    @BeforeEach
    void setUp() {
        log1 = new BitacoraAuditoria();
        log1.setIdAuditoria(1L);
        log1.setNombreTabla("usuario");
        log1.setOperacion("INSERT");
        log1.setUsuarioApp("keith");
        log1.setFechaHora(LocalDateTime.now());
        log1.setDatosNuevos("{\"id\":1}");

        log2 = new BitacoraAuditoria();
        log2.setIdAuditoria(2L);
        log2.setNombreTabla("respaldo");
        log2.setOperacion("BACKUP");
        log2.setUsuarioDb("postgres");
        log2.setFechaHora(LocalDateTime.now().minusDays(1));
        log2.setDatosAnteriores(new String(new char[250]).replace("\0", "a")); // Largo para probar el truncamiento
    }

    @Test
    void testObtenerAuditoriaConFiltros() {
        List<BitacoraAuditoria> expected = List.of(log1);
        
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(expected);

        List<BitacoraAuditoria> result = auditoriaService.obtenerAuditoria("usuario", "INSERT", "keith", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("usuario", result.get(0).getNombreTabla());
    }

    @Test
    void testObtenerAuditoriaSinFiltros() {
        List<BitacoraAuditoria> expected = List.of(log1, log2);
        
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(expected);

        List<BitacoraAuditoria> result = auditoriaService.obtenerAuditoria(null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGenerarReportePdf() throws Exception {
        List<BitacoraAuditoria> expected = List.of(log1, log2);
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(expected);

        byte[] pdfBytes = auditoriaService.generarReportePdf("usuario", "INSERT", "keith", null, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Opcional: verificar que es un PDF válido
        PdfReader reader = new PdfReader(pdfBytes);
        assertTrue(reader.getNumberOfPages() > 0);
        reader.close();
    }

    @Test
    void testGenerarReportePdfException() {
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenThrow(new RuntimeException("DB Error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            auditoriaService.generarReportePdf(null, null, null, null, null);
        });

        assertEquals("DB Error", ex.getMessage());
    }
}
