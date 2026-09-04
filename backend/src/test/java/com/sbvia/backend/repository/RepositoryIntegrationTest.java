package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.EstadoUsuario;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.TipoVia;
import com.sbvia.backend.entity.NivelDificultad;
import com.sbvia.backend.entity.TipoClima;
import com.sbvia.backend.entity.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private EscenarioRepository escenarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private SimulacionRepository simulacionRepository;

    @Autowired
    private EntityManager entityManager;

    private Rol rolUsuario;
    private EstadoUsuario estadoActivo;

    private TipoVia persistTipoVia(String nombre) {
        TipoVia tv = TipoVia.builder().nombre(nombre).build();
        entityManager.persist(tv);
        return tv;
    }

    private NivelDificultad persistNivelDificultad(Integer valor) {
        NivelDificultad nd = NivelDificultad.builder().nombre("Nivel " + valor).valor(valor).build();
        entityManager.persist(nd);
        return nd;
    }

    private TipoClima persistTipoClima(String nombre) {
        TipoClima tc = TipoClima.builder().nombre(nombre).build();
        entityManager.persist(tc);
        return tc;
    }

    @BeforeEach
    void setUp() {
        rolUsuario = rolRepository.save(
                Rol.builder().nombre("ROLE_USER").descripcion("Usuario estándar").build()
        );
        estadoActivo = entityManager.merge(EstadoUsuario.builder()
                .nombre("ACTIVO").descripcion("Cuenta habilitada").permiteAcceso(true).build());
    }

    @Test
    @DisplayName("findByActivoTrue devuelve solo escenarios activos")
    void findByActivoTrue_devuelveSoloEscenariosActivos() {
        TipoVia tv = persistTipoVia("AVENIDA");
        NivelDificultad nd = persistNivelDificultad(1);
        TipoClima tc = persistTipoClima("Soleado");

        escenarioRepository.save(Escenario.builder()
                .nombre("Av. Amazonas")
                .tipoVia(tv)
                .nivelDificultad(nd)
                .tipoClima(tc)
                .densidadTrafico("Baja")
                .activo(true)
                .build());
        escenarioRepository.save(Escenario.builder()
                .nombre("Carretera Panamericana")
                .tipoVia(tv)
                .nivelDificultad(nd)
                .tipoClima(tc)
                .densidadTrafico("Media")
                .activo(true)
                .build());
        escenarioRepository.save(Escenario.builder()
                .nombre("Escenario desactivado")
                .tipoVia(tv)
                .nivelDificultad(nd)
                .tipoClima(tc)
                .densidadTrafico("Baja")
                .activo(false)
                .build());

        Page<Escenario> resultado = escenarioRepository.findByActivoTrue(PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent())
                .extracting(Escenario::getNombre)
                .containsExactlyInAnyOrder("Av. Amazonas", "Carretera Panamericana");
    }

    @Test
    @DisplayName("findByCorreo y existsByCorreo resuelven la autenticación")
    void findByCorreo_y_existsByCorreo_resuelvenAutenticacion() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombres("Jefferson")
                .apellidos("Umaginga")
                .correo("jumagingaa@uteq.edu.ec")
                .contrasenaHash("$2a$10$hashdemo")
                .cuentaBloqueada(false)
                .rol(rolUsuario)
                .estadoUsuario(estadoActivo)
                .build());

        Optional<Usuario> encontrado = usuarioRepository.findByCorreo("jumagingaa@uteq.edu.ec");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getIdUsuario()).isEqualTo(usuario.getIdUsuario());
        assertThat(usuarioRepository.existsByCorreo("jumagingaa@uteq.edu.ec")).isTrue();
        assertThat(usuarioRepository.existsByCorreo("noexiste@sbvia.test")).isFalse();
    }

    @Test
    @DisplayName("findByUsuario_IdUsuario lista las simulaciones de un usuario")
    void findByUsuario_IdUsuario_listaSimulacionesDeUsuario() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombres("Ana")
                .apellidos("Perez")
                .correo("ana.perez@sbvia.test")
                .contrasenaHash("$2a$10$hashdemo")
                .cuentaBloqueada(false)
                .rol(rolUsuario)
                .estadoUsuario(estadoActivo)
                .build());

        TipoVia tv = persistTipoVia("CICLOVÍA");
        NivelDificultad nd = persistNivelDificultad(1);
        TipoClima tc = persistTipoClima("Soleado");

        Escenario escenario = escenarioRepository.save(Escenario.builder()
                .nombre("Ciclovía Centro")
                .tipoVia(tv)
                .nivelDificultad(nd)
                .tipoClima(tc)
                .densidadTrafico("Baja")
                .activo(true)
                .build());

        simulacionRepository.save(Simulacion.builder()
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now())
                .completada(true)
                .puntajeFinal(new BigDecimal("8.5"))
                .usuario(usuario)
                .escenario(escenario)
                .build());
        simulacionRepository.save(Simulacion.builder()
                .fechaInicio(LocalDate.now())
                .completada(false)
                .puntajeFinal(new BigDecimal("0.0"))
                .usuario(usuario)
                .escenario(escenario)
                .build());

        List<Simulacion> simulaciones = simulacionRepository
                .findByUsuario_IdUsuarioOrderByIdSimulacionDesc(usuario.getIdUsuario());

        assertThat(simulaciones).hasSize(2);
        assertThat(simulaciones)
                .extracting(Simulacion::isCompletada)
                .containsExactlyInAnyOrder(true, false);
    }
}
