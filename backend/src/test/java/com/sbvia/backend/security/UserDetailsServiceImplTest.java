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
        Usuario usuario = usuario(true);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var resultado = userDetailsService.loadUserByUsername(usuario.getEmail());

        assertThat(resultado.getUsername()).isEqualTo(usuario.getEmail());
        assertThat(resultado.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void rechazaUsuarioInactivo() {
        Usuario usuario = usuario(false);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(usuario.getEmail()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("desactivada");
    }

    @Test
    void rechazaCorreoNoRegistrado() {
        when(usuarioRepository.findByEmail("ausente@sbvia.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ausente@sbvia.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ausente@sbvia.test");
    }

    private Usuario usuario(boolean activo) {
        return Usuario.builder()
                .email("conductor@sbvia.test")
                .passwordHash("hash-seguro")
                .activo(activo)
                .rol(Rol.builder().nombre("ROLE_USER").build())
                .build();
    }
}
