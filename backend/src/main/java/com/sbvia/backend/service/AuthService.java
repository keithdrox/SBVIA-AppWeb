package com.sbvia.backend.service;

import com.sbvia.backend.dto.*;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.exception.DuplicateEmailException;
import com.sbvia.backend.repository.UsuarioRepository;
import com.sbvia.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación que gestiona registro, login, logout y refresh.
 * Delega la verificación de credenciales a Spring Security AuthenticationManager
 * y la emisión de tokens a JwtService.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Registra un nuevo usuario en el sistema.
     * Verifica que el email no exista, hashea la contraseña con BCrypt (costo 12)
     * y persiste en PostgreSQL via JPA.
     */
    @Transactional
    public AuthResponse registro(RegisterRequest request) {
        // Verificar email duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Ya existe un usuario registrado con el email: " + request.getEmail());
        }

        // Crear usuario con contraseña hasheada (BCrypt costo 12)
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.ROLE_USER)
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        // Generar tokens
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        usuario.getRol().name()))
        );

        String accessToken = jwtService.generateAccessToken(userDetails, usuario.getId(), usuario.getRol().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails, usuario.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationMs() / 1000)
                .tokenType("Bearer")
                .usuario(mapToDTO(usuario))
                .build();
    }

    /**
     * Autentica un usuario existente.
     * Delega a AuthenticationManager.authenticate() que usa UserDetailsService
     * y BCryptPasswordEncoder para verificar credenciales.
     */
    public AuthResponse login(LoginRequest request) {
        // Spring Security verifica credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String accessToken = jwtService.generateAccessToken(userDetails, usuario.getId(), usuario.getRol().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails, usuario.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationMs() / 1000)
                .tokenType("Bearer")
                .usuario(mapToDTO(usuario))
                .build();
    }

    /**
     * Cierra sesión revocando el JTI del token en Redis.
     * El JTI se agrega a la blacklist con TTL igual a la expiración restante del token.
     */
    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        long remainingMs = jwtService.getExpirationRemainingMs(token);
        if (remainingMs > 0) {
            tokenBlacklistService.blacklistToken(jti, remainingMs);
        }
    }

    /**
     * Emite un nuevo accessToken usando el refreshToken válido,
     * sin necesidad de re-autenticar con credenciales.
     */
    public AuthResponse refresh(String refreshToken) {
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("El token proporcionado no es un refresh token");
        }

        String jti = jwtService.extractJti(refreshToken);
        if (tokenBlacklistService.isTokenBlacklisted(jti)) {
            throw new IllegalArgumentException("El refresh token ha sido revocado");
        }

        String subject = jwtService.extractSubject(refreshToken);
        Usuario usuario = usuarioRepository.findById(Long.parseLong(subject))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        usuario.getRol().name()))
        );

        String newAccessToken = jwtService.generateAccessToken(userDetails, usuario.getId(), usuario.getRol().name());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationMs() / 1000)
                .tokenType("Bearer")
                .usuario(mapToDTO(usuario))
                .build();
    }

    /**
     * Obtiene los datos del usuario autenticado (sin hash de contraseña).
     */
    public UsuarioDTO getUsuarioActual(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mapToDTO(usuario);
    }

    private UsuarioDTO mapToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .activo(usuario.isActivo())
                .creadoEn(usuario.getCreadoEn())
                .build();
    }
}
