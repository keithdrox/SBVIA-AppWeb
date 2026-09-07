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

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        if (identificador == null || identificador.isBlank()) {
            throw new UsernameNotFoundException("Identificador de usuario no proporcionado");
        }

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCaseOrNombreUsuarioIgnoreCase(identificador.trim(), identificador.trim())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con identificador: " + identificador));

        if (usuario.isCuentaBloqueada()) {
            throw new UsernameNotFoundException("La cuenta del usuario está bloqueada");
        }

        return new User(
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                List.of(new SimpleGrantedAuthority(usuario.getRol().getNombre()))
        );
    }
}
