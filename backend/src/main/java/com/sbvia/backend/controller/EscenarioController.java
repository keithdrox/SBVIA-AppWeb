package com.sbvia.backend.controller;

import com.sbvia.backend.dto.EscenarioDTO;
import com.sbvia.backend.service.EscenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
     * GET /api/escenarios — Listar escenarios con paginación y filtros opcionales.
     * Accesible por cualquier usuario autenticado (ROLE_USER, ROLE_ADMIN, ROLE_INSTRUCTOR).
     * Filtros: tipoVia, nivelDificultad, clima (todos opcionales).
     */
    @GetMapping
    @Operation(summary = "Listar escenarios", description = "Lista escenarios activos con paginación y filtros opcionales (tipoVia, nivelDificultad, clima)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista devuelta exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Page<EscenarioDTO>> listar(
            @RequestParam(required = false) String tipoVia,
            @RequestParam(required = false) Integer nivelDificultad,
            @RequestParam(required = false) String clima,
            Pageable pageable) {
        Page<EscenarioDTO> page = escenarioService.buscarFiltrado(tipoVia, nivelDificultad, clima, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/escenarios/{id} — Obtener un escenario específico.
     * Accesible por cualquier usuario autenticado.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener escenario por ID", description = "Devuelve los detalles de un escenario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Escenario devuelto exitosamente"),
        @ApiResponse(responseCode = "404", description = "Escenario no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Escenario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ROLE_ADMIN)")
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Escenario actualizado"),
        @ApiResponse(responseCode = "404", description = "Escenario no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Escenario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Escenario no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        escenarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
