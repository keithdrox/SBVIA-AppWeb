package com.sbvia.backend.controller;

import com.sbvia.backend.dto.UsuarioDTO;
import com.sbvia.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de usuario autenticado.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final AuthService authService;

    /**
     * GET /api/usuarios/me — Devuelve el perfil del usuario autenticado.
     */
    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario", description = "Devuelve los datos del usuario autenticado")
    public ResponseEntity<UsuarioDTO> getPerfilActual(Authentication authentication) {
        String email = authentication.getName();
        UsuarioDTO usuario = authService.getUsuarioActual(email);
        return ResponseEntity.ok(usuario);
    }
}
