package com.sbvia.backend.controller;

import com.sbvia.backend.dto.UsuarioDTO;
import com.sbvia.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sbvia.backend.dto.CambiarRolRequest;
import jakarta.validation.Valid;

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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos devueltos exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado o token expirado")
    })
    public ResponseEntity<UsuarioDTO> getPerfilActual(Authentication authentication) {
        String email = authentication.getName();
        UsuarioDTO usuario = authService.getUsuarioActual(email);
        return ResponseEntity.ok(usuario);
    }

    /**
     * GET /api/usuarios — Lista todos los usuarios con paginación (Solo Admin).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios (Solo Admin)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista devuelta exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Page<UsuarioDTO>> listarUsuarios(Pageable pageable) {
        return ResponseEntity.ok(authService.listarUsuarios(pageable));
    }

    /**
     * PUT /api/usuarios/{id}/rol — Cambia el rol de un usuario (Solo Admin).
     */
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cambiar rol de usuario", description = "Asigna un nuevo rol a un usuario (Solo Admin)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<UsuarioDTO> cambiarRol(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarRolRequest request) {
        UsuarioDTO actualizado = authService.cambiarRol(id, request.getRol());
        return ResponseEntity.ok(actualizado);
    }

    /**
     * DELETE /api/usuarios/{id} — Desactiva un usuario (Solo Admin).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Eliminar (desactivar) usuario", description = "Soft delete de un usuario (Solo Admin)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario desactivado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        authService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
