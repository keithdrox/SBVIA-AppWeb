package com.sbvia.backend.controller;

import com.sbvia.backend.dto.*;
import com.sbvia.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

    /** Controla el flag Secure de la cookie. false en dev (HTTP), true en prod (HTTPS). */
    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * POST /api/auth/registro — Registrar nuevo usuario.
     * Devuelve el usuario creado (sin hash) y tokens JWT.
     */
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta y devuelve tokens JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya registrado")
    })
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registro(request);
        ResponseCookie cookie = ResponseCookie.from("accessToken", response.getAccessToken())
                .httpOnly(true)
                .secure(cookieSecure)        // true en producción (HTTPS), false en desarrollo
                .sameSite("Strict")          // CSRF mitigation: cookie no se envía en peticiones cross-site
                .path("/")
                .maxAge(response.getExpiresIn() / 1000)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    /**
     * POST /api/auth/login — Autenticar usuario.
     * Devuelve accessToken y refreshToken.
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica y devuelve accessToken + refreshToken")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("accessToken", response.getAccessToken())
                .httpOnly(true)
                .secure(cookieSecure)        // true en producción (HTTPS), false en desarrollo
                .sameSite("Strict")          // CSRF mitigation
                .path("/")
                .maxAge(response.getExpiresIn() / 1000)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    /**
     * POST /api/auth/logout — Cerrar sesión.
     * Agrega el JTI del token a la blacklist de Redis.
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el token JWT agregando su JTI a Redis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token inválido o ausente")
    })
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "accessToken", required = false) String cookieToken) {
        
        String token = cookieToken;
        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        if (token != null) {
            authService.logout(token);
        }

        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0) // Eliminar cookie
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * POST /api/auth/refresh — Emitir nuevo accessToken.
     * Usa el refreshToken sin re-autenticar.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Emite un nuevo accessToken usando el refreshToken")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refrescado"),
        @ApiResponse(responseCode = "403", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        ResponseCookie cookie = ResponseCookie.from("accessToken", response.getAccessToken())
                .httpOnly(true)
                .secure(cookieSecure)        // true en producción (HTTPS), false en desarrollo
                .sameSite("Strict")          // CSRF mitigation
                .path("/")
                .maxAge(response.getExpiresIn() / 1000)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}
