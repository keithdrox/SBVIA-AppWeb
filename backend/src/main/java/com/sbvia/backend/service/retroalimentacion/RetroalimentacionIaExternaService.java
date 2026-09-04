package com.sbvia.backend.service.retroalimentacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Proveedor de retroalimentación con un modelo externo compatible con la API
 * de chat completions de OpenAI. La clave viaja solo en variables de entorno
 * del backend y nunca llega al frontend ni al repositorio.
 * Cualquier fallo (sin clave, timeout, respuesta inválida) lanza
 * IaNoDisponibleException para que el orquestador use el motor local.
 */
@Service
public class RetroalimentacionIaExternaService implements ProveedorRetroalimentacion {

    public static final String ORIGEN = "IA_EXTERNA";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String proveedor;
    private final String apiUrl;
    private final String apiKey;
    private final String modelo;

    public RetroalimentacionIaExternaService(
            ObjectMapper objectMapper,
            @Value("${ia.proveedor:local}") String proveedor,
            @Value("${ia.api-url:}") String apiUrl,
            @Value("${ia.api-key:}") String apiKey,
            @Value("${ia.modelo:gpt-4o-mini}") String modelo,
            @Value("${ia.timeout-segundos:15}") int timeoutSegundos) {
        this.objectMapper = objectMapper;
        this.proveedor = proveedor;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.modelo = modelo;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSegundos * 1000);
        factory.setReadTimeout(timeoutSegundos * 1000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String origen() {
        return ORIGEN;
    }

    public boolean habilitado() {
        return "openai".equalsIgnoreCase(proveedor) && apiKey != null && !apiKey.isBlank()
                && apiUrl != null && !apiUrl.isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RetroalimentacionIaResponse generar(DatosConduccion datos) {
        if (!habilitado()) {
            throw new IaNoDisponibleException("Proveedor externo no configurado (ia.proveedor=openai + AI_API_KEY + AI_API_URL)");
        }
        try {
            Map<String, Object> cuerpo = Map.of(
                    "model", modelo,
                    "temperature", 0.3,
                    "max_tokens", 600,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "Eres un instructor de conducción. Responde SOLO con un JSON válido con las claves: "
                                            + "resumen (string), aciertos (array de strings), errores (array de strings), "
                                            + "nivelRiesgo (BAJO, MEDIO o ALTO), recomendaciones (array de exactamente 3 strings), "
                                            + "mensajeMotivador (string). Sin texto fuera del JSON."),
                            Map.of("role", "user", "content", promptUsuario(datos))));
            Map<?, ?> respuesta = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);
            return mapear(respuesta, datos);
        } catch (IaNoDisponibleException e) {
            throw e;
        } catch (Exception e) {
            throw new IaNoDisponibleException("Fallo la llamada al proveedor externo", e);
        }
    }

    private String promptUsuario(DatosConduccion d) {
        return "Evalúa esta práctica de simulación (métricas agregadas, sin datos personales): "
                + "escenario=" + d.nombreEscenario()
                + ", duracionSegundos=" + d.duracionSegundos()
                + ", velocidadPromedio=" + d.velocidadPromedio()
                + ", velocidadMaxima=" + d.velocidadMaxima()
                + ", excesos=" + d.excesosVelocidad()
                + ", colisiones=" + d.colisiones()
                + ", salidas=" + d.salidasCarril()
                + ", semaforosIgnorados=" + d.semaforosIgnorados()
                + ", semaforosRespetados=" + d.semaforosRespetados()
                + ", distanciaInsegura=" + d.distanciaInsegura()
                + ", puntaje=" + d.puntaje()
                + ", practicasPrevias=" + d.practicasPrevias();
    }

    @SuppressWarnings("unchecked")
    private RetroalimentacionIaResponse mapear(Map<?, ?> respuesta, DatosConduccion datos) {
        try {
            List<?> choices = (List<?>) respuesta.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String contenido = String.valueOf(message.get("content")).trim()
                    .replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)```\\s*$", "").trim();
            Map<?, ?> json = objectMapper.readValue(contenido, Map.class);
            return RetroalimentacionIaResponse.builder()
                    .resumen(texto(json.get("resumen"), "Práctica analizada por el modelo externo."))
                    .aciertos(lista(json.get("aciertos")))
                    .errores(lista(json.get("errores")))
                    .nivelRiesgo(nivel(json.get("nivelRiesgo")))
                    .recomendaciones(recs(json.get("recomendaciones")))
                    .puntaje(datos.puntaje())
                    .mensajeMotivador(texto(json.get("mensajeMotivador"), "Sigue practicando con constancia."))
                    .comparacion(null)
                    .origen(ORIGEN)
                    .build();
        } catch (Exception e) {
            throw new IaNoDisponibleException("Respuesta del proveedor externo inválida", e);
        }
    }

    private String texto(Object valor, String defecto) {
        return valor instanceof String s && !s.isBlank() ? s : defecto;
    }

    private List<String> lista(Object valor) {
        if (valor instanceof List<?> l) {
            return l.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private List<String> recs(Object valor) {
        List<String> base = new ArrayList<>(lista(valor));
        while (base.size() < 3) base.add("Mantener la atención plena durante todo el recorrido");
        return base.subList(0, 3);
    }

    private String nivel(Object valor) {
        String n = valor instanceof String s ? s.toUpperCase() : "";
        return "BAJO".equals(n) || "MEDIO".equals(n) || "ALTO".equals(n) ? n : "MEDIO";
    }
}
