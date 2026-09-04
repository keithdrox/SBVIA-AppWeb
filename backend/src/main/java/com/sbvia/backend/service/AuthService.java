package com.sbvia.backend.service;

import com.sbvia.backend.dto.*;
import com.sbvia.backend.entity.EstadoUsuario;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.exception.DuplicateEmailException;
import com.sbvia.backend.repository.EstadoUsuarioRepository;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EstadoUsuarioRepository estadoUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthResponse registro(RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new DuplicateEmailException(
                    "Ya existe un usuario registrado con el correo: " + request.getCorreo());
        }

        Rol rolPorDefecto = rolRepository.findByNombre("PARTICIPANTE")
                .orElseGet(() -> rolRepository.findAll().stream()
                        .filter(r -> r.getNombre().contains("PARTICIPANTE") || r.getNombre().contains("USER"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encontró el rol PARTICIPANTE")));

        // id_estado_usuario es NOT NULL: toda cuenta nueva nace en estado ACTIVO.
        EstadoUsuario estadoActivo = estadoUsuarioRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new IllegalStateException("No se encontró el estado ACTIVO"));

        Usuario usuario = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .correo(request.getCorreo())
                .telefono(request.getTelefono())
                .contrasenaHash(passwordEncoder.encode(request.getPassword()))
                .rol(rolPorDefecto)
                .estadoUsuario(estadoActivo)
                .build();

        usuario = usuarioRepository.save(usuario);

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

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreo(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
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

    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        long remainingMs = jwtService.getExpirationRemainingMs(token);
        if (remainingMs > 0) {
            tokenBlacklistService.blacklistToken(jti, remainingMs);
        }
    }

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

    public UsuarioDTO getUsuarioActual(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mapToDTO(usuario);
    }

    public org.springframework.data.domain.Page<UsuarioDTO> listarUsuarios(org.springframework.data.domain.Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToDTO);
    }

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

    @Transactional
    public UsuarioDTO actualizarUsuario(Integer id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (!usuario.getCorreo().equalsIgnoreCase(request.getCorreo())) {
            if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                throw new DuplicateEmailException("Ya existe un usuario registrado con el correo: " + request.getCorreo());
            }
            usuario.setCorreo(request.getCorreo());
        }

        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setTelefono(request.getTelefono());

        usuario = usuarioRepository.save(usuario);
        return mapToDTO(usuario);
    }

    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        usuario.setCuentaBloqueada(true);
        usuarioRepository.save(usuario);
    }

    public long getRefreshExpirationSeconds() {
        return jwtService.getRefreshExpirationMs() / 1000;
    }

    private UserDetails buildUserDetails(Usuario usuario) {
        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        usuario.getRol().getNombre()))
        );
    }

    private UsuarioDTO mapToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .nombreUsuario(usuario.getNombreUsuario())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .telefono(usuario.getTelefono())
                .cuentaBloqueada(usuario.isCuentaBloqueada())
                .build();
    }
}
