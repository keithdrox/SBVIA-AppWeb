package com.sbvia.backend.controller;

import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.dto.FinalizarSimulacionRequest;
import com.sbvia.backend.service.SimulacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/simulaciones")
@RequiredArgsConstructor
@Tag(name = "Simulaciones", description = "Endpoints de prácticas de simulaciones")
@SecurityRequirement(name = "bearerAuth")
public class SimulacionController {

    private final SimulacionService simulacionService;

    @PostMapping("/iniciar/{idEscenario}")
    @Operation(summary = "Iniciar simulación", description = "Crea una práctica en progreso para el usuario autenticado")
    public ResponseEntity<SimulacionDTO> iniciar(
            @PathVariable Integer idEscenario,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.iniciarSimulacion(authentication.getName(), idEscenario));
    }

    @PostMapping("/{idSimulacion}/finalizar")
    @Operation(summary = "Finalizar simulación", description = "Registra el puntaje y genera el resultado de la práctica")
    public ResponseEntity<SimulacionDTO> finalizar(
            @PathVariable Integer idSimulacion,
            @Valid @RequestBody FinalizarSimulacionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(simulacionService.finalizarSimulacion(
                authentication.getName(), idSimulacion, request.puntajeFinal()));
    }

    @GetMapping("/mis-practicas")
    @Operation(summary = "Obtener mis prácticas", description = "Devuelve el historial de simulaciones del usuario autenticado")
    public ResponseEntity<List<SimulacionDTO>> obtenerMisPracticas(Authentication authentication) {
        String email = authentication.getName();
        List<SimulacionDTO> practicas = simulacionService.obtenerMisPracticas(email);
        return ResponseEntity.ok(practicas);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Obtener todas las simulaciones", description = "Devuelve todas las simulaciones (requiere ROLE_ADMIN)")
    public ResponseEntity<List<SimulacionDTO>> obtenerTodas() {
        List<SimulacionDTO> practicas = simulacionService.obtenerTodas();
        return ResponseEntity.ok(practicas);
    }
}
