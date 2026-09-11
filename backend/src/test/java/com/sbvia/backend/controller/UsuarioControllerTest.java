package com.sbvia.backend.controller;

import com.sbvia.backend.dto.ActualizarUsuarioRequest;
import com.sbvia.backend.dto.CambiarRolRequest;
import com.sbvia.backend.dto.UsuarioDTO;
import com.sbvia.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UsuarioController controller;

    @Test
    void testGetPerfilActual() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");
        when(authService.getUsuarioActual("test@test.com")).thenReturn(new UsuarioDTO());

        ResponseEntity<UsuarioDTO> res = controller.getPerfilActual(auth);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testListarUsuarios() {
        Page<UsuarioDTO> page = new PageImpl<>(List.of(new UsuarioDTO()));
        when(authService.listarUsuarios(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<UsuarioDTO>> res = controller.listarUsuarios(Pageable.unpaged());
        assertEquals(200, res.getStatusCode().value());
        assertFalse(res.getBody().isEmpty());
    }

    @Test
    void testCambiarRol() {
        when(authService.cambiarRol(eq(1), any())).thenReturn(new UsuarioDTO());
        CambiarRolRequest request = new CambiarRolRequest();
        request.setNombreRol("ADMINISTRADOR");

        ResponseEntity<UsuarioDTO> res = controller.cambiarRol(1, request);
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testActualizarUsuario() {
        when(authService.actualizarUsuario(eq(1), any())).thenReturn(new UsuarioDTO());
        ResponseEntity<UsuarioDTO> res = controller.actualizarUsuario(1, new ActualizarUsuarioRequest());
        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void testEliminarUsuario() {
        doNothing().when(authService).eliminarUsuario(1);
        ResponseEntity<Void> res = controller.eliminarUsuario(1);
        assertEquals(204, res.getStatusCode().value());
    }
}
