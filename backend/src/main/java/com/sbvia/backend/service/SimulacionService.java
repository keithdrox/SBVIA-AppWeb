package com.sbvia.backend.service;

import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;

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
