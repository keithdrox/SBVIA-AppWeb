package com.sbvia.backend.controller;

import com.sbvia.backend.dto.RespaldoRequestDTO;
import com.sbvia.backend.model.Respaldo;
import com.sbvia.backend.service.RespaldoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RespaldoControllerTest {

    @Mock
    private RespaldoService respaldoService;

    @InjectMocks
    private RespaldoController controller;

    @Test
    void testListar() {
        when(respaldoService.obtenerTodos()).thenReturn(List.of(new Respaldo()));
        List<Respaldo> res = controller.listar();
        assertFalse(res.isEmpty());
    }

    @Test
    void testGenerar() {
        when(respaldoService.generarRespaldo(any(), any())).thenReturn(new Respaldo());
        Respaldo res = controller.generar(new RespaldoRequestDTO());
        assertNotNull(res);
    }

    @Test
    void testDescargar() throws IOException {
        File tempFile = File.createTempFile("test", ".backup");
        tempFile.deleteOnExit();
        when(respaldoService.obtenerArchivo(1L)).thenReturn(tempFile);
        
        ResponseEntity<Resource> res = controller.descargar(1L);
        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
    }
    
    @Test
    void testDescargarNotFound() {
        File nonExistentFile = new File("doesnotexist12345.backup");
        when(respaldoService.obtenerArchivo(1L)).thenReturn(nonExistentFile);
        
        ResponseEntity<Resource> res = controller.descargar(1L);
        assertEquals(404, res.getStatusCode().value());
    }

    @Test
    void testEliminar() {
        doNothing().when(respaldoService).eliminarRespaldo(1L);
        ResponseEntity<Void> res = controller.eliminar(1L);
        assertEquals(204, res.getStatusCode().value());
    }
}
