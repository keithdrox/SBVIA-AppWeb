package com.sbvia.backend.controller;

import com.sbvia.backend.dto.EscenarioDTO;
import com.sbvia.backend.service.EscenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el CRUD de Escenarios.
 * Demuestra el uso de Spring Data JPA con paginación y seguridad basada en roles.
 */
@RestController
@RequestMapping("/api/escenarios")
@RequiredArgsConstructor
@Tag(name = "Escenarios", description = "CRUD de escenarios de simulación vial")
@SecurityRequirement(name = "bearerAuth")
public class EscenarioController {

    private final EscenarioService escenarioService;

    /**
     * GET /api/escenarios — Listar escenarios con paginación.
     * Accesible por cualquier usuario autenticado (ROLE_USER, ROLE_ADMIN, ROLE_INSTRUCTOR).
     */
    @GetMapping
    @Operation(summary = "Listar escenarios", description = "Lista todos los escenarios activos con paginación")
    public ResponseEntity<Page<EscenarioDTO>> listar(Pageable pageable) {
        Page<EscenarioDTO> page = escenarioService.listarActivos(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/escenarios/{id} — Obtener un escenario específico.
     * Accesible por cualquier usuario autenticado.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener escenario por ID", description = "Devuelve los detalles de un escenario específico")
    public ResponseEntity<EscenarioDTO> buscarPorId(@PathVariable Integer id) {
        EscenarioDTO dto = escenarioService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/escenarios — Crear un nuevo escenario.
     * Solo accesible por administradores.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Crear escenario", description = "Crea un nuevo escenario. Requiere ROLE_ADMIN")
    public ResponseEntity<EscenarioDTO> crear(@Valid @RequestBody EscenarioDTO dto) {
        EscenarioDTO creado = escenarioService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * PUT /api/escenarios/{id} — Actualizar un escenario.
     * Solo accesible por administradores.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Actualizar escenario", description = "Actualiza un escenario existente. Requiere ROLE_ADMIN")
    public ResponseEntity<EscenarioDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EscenarioDTO dto) {
        EscenarioDTO actualizado = escenarioService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * DELETE /api/escenarios/{id} — Eliminar (soft delete) un escenario.
     * Solo accesible por administradores.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Eliminar escenario", description = "Soft delete de un escenario. Requiere ROLE_ADMIN")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        escenarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
