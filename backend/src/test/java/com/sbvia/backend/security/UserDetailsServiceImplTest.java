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
    void cargaUsuarioActivoConSuRol() {
        Usuario usuario = usuario(false);
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        var resultado = userDetailsService.loadUserByUsername(usuario.getCorreo());

        assertThat(resultado.getUsername()).isEqualTo(usuario.getCorreo());
        assertThat(resultado.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void rechazaUsuarioInactivo() {
        Usuario usuario = usuario(true);
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(usuario.getCorreo()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("bloqueada");
    }

    @Test
    void rechazaCorreoNoRegistrado() {
        when(usuarioRepository.findByCorreo("ausente@sbvia.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ausente@sbvia.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ausente@sbvia.test");
    }

    private Usuario usuario(boolean cuentaBloqueada) {
        return Usuario.builder()
                .correo("conductor@sbvia.test")
                .contrasenaHash("hash-seguro")
                .cuentaBloqueada(cuentaBloqueada)
                .rol(Rol.builder().nombre("ROLE_USER").build())
                .build();
    }
}
