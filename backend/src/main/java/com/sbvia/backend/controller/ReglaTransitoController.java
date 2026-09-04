package com.sbvia.backend.controller;

import com.sbvia.backend.dto.ReglaTransitoDTO;
import com.sbvia.backend.service.ReglaTransitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reglas-transito")
@RequiredArgsConstructor
@Tag(name = "Reglas de tránsito", description = "Administración de normativas del motor de evaluación")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class ReglaTransitoController {
    private final ReglaTransitoService reglaService;

    @GetMapping
    @Operation(summary = "Listar reglas de tránsito")
    public List<ReglaTransitoDTO> listar() { return reglaService.listar(); }

    @PostMapping
    @Operation(summary = "Registrar una regla de tránsito")
    public ResponseEntity<ReglaTransitoDTO> crear(@Valid @RequestBody ReglaTransitoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reglaService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una regla de tránsito")
    public ReglaTransitoDTO actualizar(@PathVariable Integer id, @Valid @RequestBody ReglaTransitoDTO dto) {
        return reglaService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una regla de tránsito")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        reglaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
