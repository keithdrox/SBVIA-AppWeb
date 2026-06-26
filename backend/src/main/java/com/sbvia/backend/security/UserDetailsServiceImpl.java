package com.sbvia.backend.security;

import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de UserDetailsService que carga usuarios desde PostgreSQL
 * a través de UsuarioRepository (Spring Data JPA).
 * Spring Security usa este servicio para autenticar usuarios.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por email (usado como username en el sistema).
     * Consulta: UsuarioRepository.findByEmail() → PostgreSQL.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("La cuenta del usuario está desactivada");
        }

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(usuario.getRol().name()))
        );
    }
}
