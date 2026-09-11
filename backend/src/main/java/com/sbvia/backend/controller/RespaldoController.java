package com.sbvia.backend.controller;

import com.sbvia.backend.model.Respaldo;
import com.sbvia.backend.service.RespaldoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/respaldos")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class RespaldoController {

    private final RespaldoService respaldoService;

    public RespaldoController(RespaldoService respaldoService) {
        this.respaldoService = respaldoService;
    }

    @GetMapping
    public List<Respaldo> listar() {
        return respaldoService.obtenerTodos();
    }

    @PostMapping("/generar")
    public Respaldo generar(@RequestBody com.sbvia.backend.dto.RespaldoRequestDTO request) {
        return respaldoService.generarRespaldo(request, "MANUAL");
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        File file = respaldoService.obtenerArchivo(id);
        
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        respaldoService.eliminarRespaldo(id);
        return ResponseEntity.noContent().build();
    }
}
