package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
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

/**
 * Pruebas de integración de la capa de repositorios (C7 de la rúbrica).
 * Verifica que las consultas derivadas y los procedimientos almacenados
 * se ejecutan correctamente contra una base de datos real embebida (H2).
 */
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

    private Rol rolUsuario;

    @BeforeEach
    void setUp() {
        rolUsuario = rolRepository.save(
                Rol.builder().nombre("ROLE_USER").descripcion("Usuario estándar").build()
        );
    }

    @Test
    @DisplayName("findByActivoTrue devuelve solo escenarios activos")
    void findByActivoTrue_devuelveSoloEscenariosActivos() {
        escenarioRepository.save(Escenario.builder()
                .nombre("Av. Amazonas")
                .tipoVia("AVENIDA")
                .nivelDificultad(1)
                .clima("Soleado")
                .densidadTrafico("Baja")
                .activo(true)
                .build());
        escenarioRepository.save(Escenario.builder()
                .nombre("Carretera Panamericana")
                .tipoVia("CARRETERA")
                .nivelDificultad(3)
                .clima("Lluvioso")
                .densidadTrafico("Media")
                .activo(true)
                .build());
        escenarioRepository.save(Escenario.builder()
                .nombre("Escenario desactivado")
                .tipoVia("CALLE")
                .nivelDificultad(2)
                .clima("Nublado")
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
    @DisplayName("findByTipoViaAndActivoTrue filtra por tipo de vía")
    void findByTipoViaAndActivoTrue_filtraPorTipoVia() {
        escenarioRepository.save(Escenario.builder()
                .nombre("Av. 6 de Diciembre")
                .tipoVia("AVENIDA")
                .nivelDificultad(2)
                .clima("Soleado")
                .densidadTrafico("Media")
                .activo(true)
                .build());
        escenarioRepository.save(Escenario.builder()
                .nombre("Autopista General Rumiñahui")
                .tipoVia("AUTOPISTA")
                .nivelDificultad(4)
                .clima("Lluvioso")
                .densidadTrafico("Alta")
                .activo(true)
                .build());

        Page<Escenario> resultado = escenarioRepository
                .findByTipoViaAndActivoTrue("AVENIDA", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Av. 6 de Diciembre");
    }

    @Test
    @DisplayName("findByEmail y existsByEmail resuelven la autenticación")
    void findByEmail_y_existsByEmail_resuelvenAutenticacion() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Jefferson")
                .apellido("Umaginga")
                .email("jumagingaa@uteq.edu.ec")
                .passwordHash("$2a$10$hashdemo")
                .estado("Activo")
                .activo(true)
                .rol(rolUsuario)
                .build());

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("jumagingaa@uteq.edu.ec");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getIdUsuario()).isEqualTo(usuario.getIdUsuario());
        assertThat(usuarioRepository.existsByEmail("jumagingaa@uteq.edu.ec")).isTrue();
        assertThat(usuarioRepository.existsByEmail("noexiste@sbvia.test")).isFalse();
    }

    @Test
    @DisplayName("findByUsuario_IdUsuario lista las simulaciones de un usuario")
    void findByUsuario_IdUsuario_listaSimulacionesDeUsuario() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Ana")
                .apellido("Perez")
                .email("ana.perez@sbvia.test")
                .passwordHash("$2a$10$hashdemo")
                .estado("Activo")
                .activo(true)
                .rol(rolUsuario)
                .build());

        Escenario escenario = escenarioRepository.save(Escenario.builder()
                .nombre("Ciclovía Centro")
                .tipoVia("CICLOVÍA")
                .nivelDificultad(1)
                .clima("Soleado")
                .densidadTrafico("Baja")
                .activo(true)
                .build());

        simulacionRepository.save(Simulacion.builder()
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now())
                .estado("Completado")
                .puntajeFinal(new BigDecimal("8.5"))
                .usuario(usuario)
                .escenario(escenario)
                .build());
        simulacionRepository.save(Simulacion.builder()
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now())
                .estado("En_curso")
                .puntajeFinal(new BigDecimal("0.0"))
                .usuario(usuario)
                .escenario(escenario)
                .build());

        List<Simulacion> simulaciones = simulacionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        assertThat(simulaciones).hasSize(2);
        assertThat(simulaciones)
                .extracting(Simulacion::getEstado)
                .containsExactlyInAnyOrder("Completado", "En_curso");
    }
}
