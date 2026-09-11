package com.sbvia.backend.service;

import com.sbvia.backend.dto.RespaldoRequestDTO;
import com.sbvia.backend.entity.BitacoraAuditoria;
import com.sbvia.backend.model.Respaldo;
import com.sbvia.backend.repository.BitacoraAuditoriaRepository;
import com.sbvia.backend.repository.RespaldoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RespaldoServiceTest {

    @Mock
    private RespaldoRepository respaldoRepository;

    @Mock
    private BitacoraAuditoriaRepository auditoriaRepository;

    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private RespaldoService respaldoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(respaldoService, "dbUser", "postgres");
        ReflectionTestUtils.setField(respaldoService, "dbPassword", "pass");
        ReflectionTestUtils.setField(respaldoService, "dbUrl", "jdbc:postgresql://postgres:5432/sbvia_db");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testObtenerTodos() {
        Respaldo r = new Respaldo();
        r.setIdRespaldo(1L);
        when(respaldoRepository.findAllByOrderByFechaInicioDesc()).thenReturn(List.of(r));

        List<Respaldo> result = respaldoService.obtenerTodos();
        assertEquals(1, result.size());
    }

    @Test
    void testGenerarRespaldoInmediato() {
        RespaldoRequestDTO dto = new RespaldoRequestDTO();
        dto.setModalidad("COMPLETO");
        dto.setComentario("Test comment");

        when(respaldoRepository.save(any(Respaldo.class))).thenAnswer(inv -> {
            Respaldo r = inv.getArgument(0);
            if (r.getIdRespaldo() == null) r.setIdRespaldo(1L);
            return r;
        });

        // Mock ProcessBuilder (to avoid real execution in test)
        RespaldoService spyService = spy(respaldoService);
        doNothing().when(spyService).ejecutarPgDump(any(Respaldo.class));

        Respaldo result = spyService.generarRespaldo(dto, "MANUAL");

        assertNotNull(result);
        assertEquals("EN_PROGRESO", result.getEstado());
        assertEquals("COMPLETO", result.getModalidad());
        assertEquals("MANUAL", result.getTipo());
        verify(spyService).ejecutarPgDump(result);
        verify(auditoriaRepository).save(any(BitacoraAuditoria.class));
    }

    @Test
    void testGenerarRespaldoProgramado() {
        RespaldoRequestDTO dto = new RespaldoRequestDTO();
        dto.setModalidad("SOLO_ESTRUCTURA");
        dto.setFechaProgramada(LocalDateTime.now().plusDays(1));

        when(respaldoRepository.save(any(Respaldo.class))).thenAnswer(inv -> {
            Respaldo r = inv.getArgument(0);
            if (r.getIdRespaldo() == null) r.setIdRespaldo(1L);
            return r;
        });

        Respaldo result = respaldoService.generarRespaldo(dto, "MANUAL");

        assertEquals("PROGRAMADO", result.getEstado());
        assertNotNull(result.getFechaProgramada());
        verify(taskScheduler).schedule(any(Runnable.class), any(Date.class));
    }

    @Test
    void testObtenerArchivo() {
        Respaldo r = new Respaldo();
        r.setIdRespaldo(1L);
        r.setNombreArchivo("test.backup");
        when(respaldoRepository.findById(1L)).thenReturn(Optional.of(r));

        File file = respaldoService.obtenerArchivo(1L);
        assertNotNull(file);
        assertEquals("test.backup", file.getName());
    }

    @Test
    void testEliminarRespaldo() {
        Respaldo r = new Respaldo();
        r.setIdRespaldo(1L);
        r.setNombreArchivo("dummy.backup");
        when(respaldoRepository.findById(1L)).thenReturn(Optional.of(r));

        respaldoService.eliminarRespaldo(1L);
        verify(respaldoRepository).delete(r);
    }

    @Test
    void testRespaldoProgramadoCron() {
        RespaldoService spyService = spy(respaldoService);
        doReturn(new Respaldo()).when(spyService).generarRespaldo(any(RespaldoRequestDTO.class), eq("PROGRAMADO"));

        spyService.respaldoProgramado();

        ArgumentCaptor<RespaldoRequestDTO> captor = ArgumentCaptor.forClass(RespaldoRequestDTO.class);
        verify(spyService).generarRespaldo(captor.capture(), eq("PROGRAMADO"));
        assertEquals("COMPLETO", captor.getValue().getModalidad());
    }
}
