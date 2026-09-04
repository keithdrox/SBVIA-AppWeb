package com.sbvia.backend.service.retroalimentacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetroalimentacionIaExternaServiceTest {

    private HttpServer servidor;

    @AfterEach
    void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    private DatosConduccion datos() {
        return new DatosConduccion(100, new BigDecimal("45.00"), new BigDecimal("70.00"),
                1, 0, 0, 0, 1, 0, new BigDecimal("95.00"), "Centro urbano", 0,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private RetroalimentacionIaExternaService servicio(String url) {
        return new RetroalimentacionIaExternaService(new ObjectMapper(), "openai", url,
                "clave-de-prueba", "modelo-test", 5);
    }

    private void responder(String cuerpo, String contentType) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        servidor.createContext("/chat", intercambio -> {
            intercambio.getResponseHeaders().set("Content-Type", contentType);
            intercambio.sendResponseHeaders(200, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
    }

    private String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/chat";
    }

    @Test
    void interpretaLaRespuestaJsonDelProveedor() throws Exception {
        responder("{\"choices\":[{\"message\":{\"content\":"
                + "\"```json\\n{\\\"resumen\\\":\\\"Buen manejo\\\","
                + "\\\"aciertos\\\":[\\\"Respeta límites\\\"],"
                + "\\\"errores\\\":[],"
                + "\\\"nivelRiesgo\\\":\\\"BAJO\\\","
                + "\\\"recomendaciones\\\":[\\\"Uno\\\",\\\"Dos\\\",\\\"Tres\\\"],"
                + "\\\"mensajeMotivador\\\":\\\"Sigue así\\\"}\\n```\"}}]}",
                "application/json");

        RetroalimentacionIaResponse informe = servicio(url()).generar(datos());

        assertThat(informe.getOrigen()).isEqualTo("IA_EXTERNA");
        assertThat(informe.getResumen()).isEqualTo("Buen manejo");
        assertThat(informe.getNivelRiesgo()).isEqualTo("BAJO");
        assertThat(informe.getRecomendaciones()).containsExactly("Uno", "Dos", "Tres");
        // El puntaje siempre lo impone el servidor, nunca el modelo externo.
        assertThat(informe.getPuntaje()).isEqualByComparingTo("95.00");
    }

    @Test
    void lanzaExcepcionCuandoLaRespuestaEsInvalida() throws Exception {
        responder("{\"choices\":[]}", "application/json");

        assertThatThrownBy(() -> servicio(url()).generar(datos()))
                .isInstanceOf(IaNoDisponibleException.class);
    }

    @Test
    void lanzaExcepcionCuandoNoEstaConfigurado() {
        RetroalimentacionIaExternaService sinClave = new RetroalimentacionIaExternaService(
                new ObjectMapper(), "local", "", "", "modelo-test", 5);

        assertThat(sinClave.habilitado()).isFalse();
        assertThatThrownBy(() -> sinClave.generar(datos()))
                .isInstanceOf(IaNoDisponibleException.class);
    }
}
