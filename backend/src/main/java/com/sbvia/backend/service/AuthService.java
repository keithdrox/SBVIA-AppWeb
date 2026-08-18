package com.sbvia.backend.service;

import com.sbvia.backend.dto.*;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.exception.DuplicateEmailException;
import com.sbvia.backend.repository.UsuarioRepository;
import com.sbvia.backend.repository.RolRepository;
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
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Registra un nuevo usuario en el sistema.
     * Verifica que el email no exista, hashea la contraseña con BCrypt
     * y persiste en PostgreSQL via JPA.
     */
    @Transactional
    public AuthResponse registro(RegisterRequest request) {
        // Verificar email duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Ya existe un usuario registrado con el email: " + request.getEmail());
        }

        // Usar el nombre canónico reconocido por las reglas de Spring Security.
        Rol rolPorDefecto = rolRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol ROLE_USER"));

        // Crear usuario con contraseña hasheada (BCrypt)
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .tipoLicencia(request.getTipoLicencia())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rolPorDefecto)
                .estado("Activo")
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        // Generar tokens
        UserDetails userDetails = buildUserDetails(usuario);
        String rolNombre = usuario.getRol().getNombre();
        String accessToken = jwtService.generateAccessToken(userDetails, usuario.getIdUsuario().longValue(), rolNombre);
        String refreshToken = jwtService.generateRefreshToken(userDetails, usuario.getIdUsuario().longValue());

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
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String rolNombre = usuario.getRol().getNombre();
        String accessToken = jwtService.generateAccessToken(userDetails, usuario.getIdUsuario().longValue(), rolNombre);
        String refreshToken = jwtService.generateRefreshToken(userDetails, usuario.getIdUsuario().longValue());

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
     */
    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        long remainingMs = jwtService.getExpirationRemainingMs(token);
        if (remainingMs > 0) {
            tokenBlacklistService.blacklistToken(jti, remainingMs);
        }
    }

    /**
     * Emite un nuevo accessToken usando el refreshToken válido.
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
        Usuario usuario = usuarioRepository.findById(Integer.parseInt(subject))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        UserDetails userDetails = buildUserDetails(usuario);
        String rolNombre = usuario.getRol().getNombre();
        String newAccessToken = jwtService.generateAccessToken(userDetails, usuario.getIdUsuario().longValue(), rolNombre);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationMs() / 1000)
                .tokenType("Bearer")
                .usuario(mapToDTO(usuario))
                .build();
    }

    /**
     * Obtiene los datos del usuario autenticado.
     */
    public UsuarioDTO getUsuarioActual(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mapToDTO(usuario);
    }

    /**
     * Lista todos los usuarios con paginación.
     */
    public org.springframework.data.domain.Page<UsuarioDTO> listarUsuarios(org.springframework.data.domain.Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToDTO);
    }

    /**
     * Cambia el rol de un usuario existente.
     */
    @Transactional
    public UsuarioDTO cambiarRol(Integer id, String nombreRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        Rol nuevoRol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombreRol));

        usuario.setRol(nuevoRol);
        usuario = usuarioRepository.save(usuario);
        return mapToDTO(usuario);
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    @Transactional
    public UsuarioDTO actualizarUsuario(Integer id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        // Verificar email duplicado solo si cambió
        if (!usuario.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException("Ya existe un usuario registrado con el email: " + request.getEmail());
            }
            usuario.setEmail(request.getEmail());
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setTelefono(request.getTelefono());
        usuario.setTipoLicencia(request.getTipoLicencia());
        usuario.setCedula(request.getCedula());
        usuario.setTipoSangre(request.getTipoSangre());
        usuario.setDiscapacidad(request.getDiscapacidad());

        usuario = usuarioRepository.save(usuario);
        return mapToDTO(usuario);
    }

    /**
     * Elimina un usuario (soft delete).
     */
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(false);
        usuario.setEstado("Inactivo");
        usuarioRepository.save(usuario);
    }

    public long getRefreshExpirationSeconds() {
        return jwtService.getRefreshExpirationMs() / 1000;
    }

    private UserDetails buildUserDetails(Usuario usuario) {
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        usuario.getRol().getNombre()))
        );
    }

    private UsuarioDTO mapToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .tipoLicencia(usuario.getTipoLicencia())
                .cedula(usuario.getCedula())
                .tipoSangre(usuario.getTipoSangre())
                .discapacidad(usuario.getDiscapacidad())
                .rol(usuario.getRol().getNombre())
                .activo(usuario.isActivo())
                .creadoEn(usuario.getCreadoEn())
                .build();
    }
}
