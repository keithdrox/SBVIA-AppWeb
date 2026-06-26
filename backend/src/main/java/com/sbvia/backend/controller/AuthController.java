package com.sbvia.backend.controller;

import com.sbvia.backend.dto.*;
import com.sbvia.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación: registro, login, logout y refresh token.
 * Endpoints públicos: /api/auth/registro, /api/auth/login
 * Endpoints protegidos: /api/auth/logout, /api/auth/refresh, /api/usuarios/me
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de registro, login, logout y refresh token JWT")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/registro — Registrar nuevo usuario.
     * Devuelve el usuario creado (sin hash) y tokens JWT.
     */
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta y devuelve tokens JWT")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login — Autenticar usuario.
     * Devuelve accessToken y refreshToken.
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica y devuelve accessToken + refreshToken")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout — Cerrar sesión.
     * Agrega el JTI del token a la blacklist de Redis.
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el token JWT agregando su JTI a Redis")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/auth/refresh — Emitir nuevo accessToken.
     * Usa el refreshToken sin re-autenticar.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Emite un nuevo accessToken usando el refreshToken")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
