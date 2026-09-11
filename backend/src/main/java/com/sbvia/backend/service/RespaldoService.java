package com.sbvia.backend.service;

import com.sbvia.backend.model.Respaldo;
import com.sbvia.backend.repository.RespaldoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class RespaldoService {

    private static final Logger logger = LoggerFactory.getLogger(RespaldoService.class);

    private final RespaldoRepository respaldoRepository;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Confiamos en que Spring Boot resuelve el nombre del host (postgres) gracias al docker-compose
    @Value("${DB_URL:jdbc:postgresql://postgres:5432/sbvia_db}")
    private String dbUrl;

    private final String backupDir = "/app/backups";

    public RespaldoService(RespaldoRepository respaldoRepository) {
        this.respaldoRepository = respaldoRepository;
        // Crear directorio si no existe
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<Respaldo> obtenerTodos() {
        return respaldoRepository.findAllByOrderByFechaInicioDesc();
    }

    public Respaldo generarRespaldo(String tipo) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "sbvia_backup_" + timestamp + ".backup";

        Respaldo respaldo = new Respaldo();
        respaldo.setNombreArchivo(filename);
        respaldo.setTipo(tipo);
        respaldo.setEstado("EN_PROGRESO");
        respaldo.setFechaInicio(LocalDateTime.now());

        respaldo = respaldoRepository.save(respaldo);

        // Ejecutar pg_dump asíncronamente
        ejecutarPgDump(respaldo);

        return respaldo;
    }

    @Async
    protected void ejecutarPgDump(Respaldo respaldo) {
        String outputPath = backupDir + "/" + respaldo.getNombreArchivo();
        
        // Parsear URL de DB. Ejemplo: jdbc:postgresql://postgres:5432/sbvia_db
        // Extraemos host y base de datos (simplificado para este entorno)
        String host = "postgres"; 
        String dbName = "sbvia_db";
        
        if (dbUrl.contains("://")) {
            String cleanUrl = dbUrl.substring(dbUrl.indexOf("://") + 3);
            if (cleanUrl.contains("/")) {
                String[] parts = cleanUrl.split("/");
                host = parts[0].split(":")[0];
                dbName = parts[1].split("\\?")[0];
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-U", dbUser,
                "-d", dbName,
                "-F", "c", // Custom format
                "-f", outputPath
        );

        // Inyectar contraseña segura
        Map<String, String> env = processBuilder.environment();
        env.put("PGPASSWORD", dbPassword);

        try {
            logger.info("Iniciando respaldo de base de datos: {}", outputPath);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            respaldo.setFechaFin(LocalDateTime.now());

            if (exitCode == 0) {
                File file = new File(outputPath);
                if (file.exists()) {
                    respaldo.setTamanioBytes(file.length());
                    respaldo.setEstado("COMPLETADO");
                    respaldo.setDetalles("Respaldo completado exitosamente.");
                    logger.info("Respaldo completado: {}", outputPath);
                } else {
                    respaldo.setEstado("FALLIDO");
                    respaldo.setDetalles("Archivo no encontrado tras finalizar pg_dump.");
                }
            } else {
                respaldo.setEstado("FALLIDO");
                respaldo.setDetalles("pg_dump devolvió código de error: " + exitCode);
                logger.error("Error al ejecutar pg_dump. Código de salida: {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            respaldo.setEstado("FALLIDO");
            respaldo.setFechaFin(LocalDateTime.now());
            respaldo.setDetalles("Excepción: " + e.getMessage());
            logger.error("Excepción durante respaldo", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        respaldoRepository.save(respaldo);
    }

    public File obtenerArchivo(Long id) {
        Respaldo respaldo = respaldoRepository.findById(id).orElseThrow(() -> new RuntimeException("Respaldo no encontrado"));
        return new File(backupDir + "/" + respaldo.getNombreArchivo());
    }

    public void eliminarRespaldo(Long id) {
        Respaldo respaldo = respaldoRepository.findById(id).orElseThrow(() -> new RuntimeException("Respaldo no encontrado"));
        File file = new File(backupDir + "/" + respaldo.getNombreArchivo());
        if (file.exists()) {
            file.delete();
        }
        respaldoRepository.delete(respaldo);
    }

    // Respaldo automático todos los días a las 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void respaldoProgramado() {
        logger.info("Ejecutando respaldo automático programado...");
        generarRespaldo("PROGRAMADO");
    }
}
