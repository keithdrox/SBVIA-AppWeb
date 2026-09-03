package com.sbvia.backend.service;

import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscenarioRepository escenarioRepository;

    public SimulacionDTO iniciarSimulacion(String email, Integer idEscenario) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Escenario escenario = escenarioRepository.findById(idEscenario)
                .filter(Escenario::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Escenario activo no encontrado"));

        LocalDate hoy = LocalDate.now();
        Simulacion simulacion = Simulacion.builder()
                .fechaInicio(hoy)
                .fechaFin(hoy)
                .estado("EN_PROGRESO")
                .puntajeFinal(BigDecimal.ZERO)
                .usuario(usuario)
                .escenario(escenario)
                .build();
        return mapToDTO(simulacionRepository.save(simulacion));
    }

    public SimulacionDTO finalizarSimulacion(String email, Integer idSimulacion, BigDecimal puntajeFinal) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada"));
        if (!simulacion.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("La simulación pertenece a otro usuario");
        }
        if (!"EN_PROGRESO".equals(simulacion.getEstado())) {
            throw new IllegalArgumentException("La simulación ya fue finalizada");
        }

        simulacion.setFechaFin(LocalDate.now());
        simulacion.setPuntajeFinal(puntajeFinal);
        simulacion.setEstado(puntajeFinal.compareTo(new BigDecimal("70")) >= 0 ? "APROBADA" : "REPROBADA");
        return mapToDTO(simulacionRepository.save(simulacion));
    }

    public List<SimulacionDTO> obtenerMisPracticas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Simulacion> simulaciones = simulacionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        return simulaciones.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SimulacionDTO> obtenerTodas() {
        return StreamSupport.stream(simulacionRepository.findAll().spliterator(), false)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SimulacionDTO mapToDTO(Simulacion simulacion) {
        return SimulacionDTO.builder()
                .idSimulacion(simulacion.getIdSimulacion())
                .fechaInicio(simulacion.getFechaInicio())
                .fechaFin(simulacion.getFechaFin())
                .estado(simulacion.getEstado())
                .puntajeFinal(simulacion.getPuntajeFinal())
                .idEscenario(simulacion.getEscenario() != null ? simulacion.getEscenario().getIdEscenario() : null)
                .nombreEscenario(simulacion.getEscenario() != null ? simulacion.getEscenario().getNombre() : "N/A")
                .build();
    }
}
