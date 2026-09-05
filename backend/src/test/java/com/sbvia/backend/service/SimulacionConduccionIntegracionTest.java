package com.sbvia.backend.service;

import com.sbvia.backend.dto.MetricasConduccionRequest;
import com.sbvia.backend.dto.ResultadoConduccionDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.EstadoSimulacion;
import com.sbvia.backend.entity.EstadoUsuario;
import com.sbvia.backend.entity.NivelDificultad;
import com.sbvia.backend.entity.NivelGravedad;
import com.sbvia.backend.entity.ReglaTransito;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.TipoClima;
import com.sbvia.backend.entity.TipoMetrica;
import com.sbvia.backend.entity.TipoVia;
import com.sbvia.backend.entity.TipoVehiculo;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.entity.Vehiculo;
import com.sbvia.backend.repository.InfraccionRepository;
import com.sbvia.backend.repository.MetricaDesempenoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que finalizarConduccion persiste simulación, métricas e infracciones
 * en una base real (H2) dentro de una sola transacción.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SimulacionConduccionIntegracionTest {

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private MetricaDesempenoRepository metricaDesempenoRepository;

    @Autowired
    private InfraccionRepository infraccionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void finalizaConduccionYPersisteMetricasEInfracciones() {
        Rol rol = Rol.builder().nombre("PARTICIPANTE").descripcion("Conductor").build();
        entityManager.persist(rol);
        EstadoUsuario activo = EstadoUsuario.builder().nombre("ACTIVO").descripcion("Habilitada").build();
        entityManager.persist(activo);
        Usuario usuario = Usuario.builder()
                .nombres("Test").apellidos("Conductor").correo("conductor.it@sbvia.test")
                .contrasenaHash("hash").rol(rol).estadoUsuario(activo).build();
        entityManager.persist(usuario);
        TipoVia via = TipoVia.builder().nombre("Urbana").build();
        entityManager.persist(via);
        NivelDificultad nivel = NivelDificultad.builder().nombre("Intermedio").valor(2).build();
        entityManager.persist(nivel);
        TipoClima clima = TipoClima.builder().nombre("Despejado").build();
        entityManager.persist(clima);
        Escenario escenario = Escenario.builder()
                .nombre("Pista IT").densidadTrafico("MEDIA")
                .tipoVia(via).nivelDificultad(nivel).tipoClima(clima).build();
        entityManager.persist(escenario);
        TipoVehiculo tipoVehiculo = TipoVehiculo.builder()
                .nombre("AUTOMOVIL").licenciaRequerida("B").build();
        entityManager.persist(tipoVehiculo);
        Vehiculo vehiculo = Vehiculo.builder()
                .nombre("Vehículo IT").transmision("MANUAL")
                .tipoVehiculo(tipoVehiculo).build();
        entityManager.persist(vehiculo);
        Simulacion simulacion = Simulacion.builder()
                .usuario(usuario).escenario(escenario).vehiculo(vehiculo)
                .puntajeFinal(BigDecimal.ZERO).build();
        entityManager.persist(simulacion);
        entityManager.persist(EstadoSimulacion.builder().nombre("COMPLETADA").build());
        entityManager.persist(TipoMetrica.builder().nombre("VELOCIDAD_PROMEDIO").build());
        entityManager.persist(TipoMetrica.builder().nombre("TOTAL_INFRACCIONES").build());
        entityManager.persist(TipoMetrica.builder().nombre("PUNTAJE_SEGURIDAD").build());
        entityManager.persist(TipoMetrica.builder().nombre("PORCENTAJE_CUMPLIMIENTO").build());
        entityManager.persist(ReglaTransito.builder().codigo("RT-001").nombre("Semáforo")
                .categoria("SENALIZACION").penalizacionBase(new BigDecimal("20.00")).build());
        entityManager.persist(ReglaTransito.builder().codigo("RT-002").nombre("Velocidad")
                .categoria("VELOCIDAD").penalizacionBase(new BigDecimal("15.00")).build());
        entityManager.persist(NivelGravedad.builder().nombre("MODERADA").valor(4).build());
        entityManager.persist(NivelGravedad.builder().nombre("GRAVE").valor(7).build());
        entityManager.flush();

        MetricasConduccionRequest metricas = new MetricasConduccionRequest(
                120, new BigDecimal("45.50"), new BigDecimal("72.00"), 2, 1, 1, 1, 1, 0);
        ResultadoConduccionDTO resultado = simulacionService.finalizarConduccion(
                "conductor.it@sbvia.test", simulacion.getIdSimulacion(), metricas);

        // 100 - (2*15 + 1*20 + 1*20 + 1*10 + 1*8) = 12
        assertThat(resultado.getSimulacion().getPuntajeFinal()).isEqualByComparingTo("12.00");
        assertThat(resultado.getRetroalimentacion().getNivelRiesgo()).isEqualTo("ALTO");
        assertThat(resultado.getRetroalimentacion().getRecomendaciones()).hasSize(3);
        assertThat(resultado.getRetroalimentacion().getOrigen()).isEqualTo("IA_LOCAL");
        assertThat(metricaDesempenoRepository.findBySimulacion_IdSimulacion(simulacion.getIdSimulacion()))
                .hasSize(4);
        assertThat(infraccionRepository.findBySimulacion_IdSimulacion(simulacion.getIdSimulacion()))
                .hasSize(2);
        entityManager.flush();
        entityManager.clear();
        Simulacion recargada = entityManager.find(Simulacion.class, simulacion.getIdSimulacion());
        assertThat(recargada.isCompletada()).isTrue();
        assertThat(recargada.getDuracionSegundos()).isEqualTo(120);
        assertThat(recargada.getEstadoSimulacion().getNombre()).isEqualTo("COMPLETADA");
    }
}
