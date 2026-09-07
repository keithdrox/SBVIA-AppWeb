package com.sbvia.backend.security;

import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void cargaUsuarioActivoPorCorreo() {
        Usuario usuario = usuario(false);
        when(usuarioRepository.findByCorreoIgnoreCaseOrNombreUsuarioIgnoreCase(usuario.getCorreo(), usuario.getCorreo()))
                .thenReturn(Optional.of(usuario));

        var resultado = userDetailsService.loadUserByUsername(usuario.getCorreo());

        assertThat(resultado.getUsername()).isEqualTo(usuario.getCorreo());
        assertThat(resultado.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void cargaUsuarioActivoPorNombreUsuario() {
        Usuario usuario = usuario(false);
        when(usuarioRepository.findByCorreoIgnoreCaseOrNombreUsuarioIgnoreCase(usuario.getNombreUsuario(), usuario.getNombreUsuario()))
                .thenReturn(Optional.of(usuario));

        var resultado = userDetailsService.loadUserByUsername(usuario.getNombreUsuario());

        assertThat(resultado.getUsername()).isEqualTo(usuario.getCorreo());
        assertThat(resultado.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void rechazaUsuarioInactivo() {
        Usuario usuario = usuario(true);
        when(usuarioRepository.findByCorreoIgnoreCaseOrNombreUsuarioIgnoreCase(usuario.getCorreo(), usuario.getCorreo()))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(usuario.getCorreo()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("bloqueada");
    }

    @Test
    void rechazaIdentificadorNoRegistrado() {
        when(usuarioRepository.findByCorreoIgnoreCaseOrNombreUsuarioIgnoreCase("ausente@sbvia.test", "ausente@sbvia.test"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ausente@sbvia.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ausente@sbvia.test");
    }

    private Usuario usuario(boolean cuentaBloqueada) {
        return Usuario.builder()
                .nombres("Conductor")
                .apellidos("Demo")
                .nombreUsuario("cdemop")
                .correo("conductor@sbvia.test")
                .contrasenaHash("hash-seguro")
                .cuentaBloqueada(cuentaBloqueada)
                .rol(Rol.builder().nombre("ROLE_USER").build())
                .build();
    }
}
