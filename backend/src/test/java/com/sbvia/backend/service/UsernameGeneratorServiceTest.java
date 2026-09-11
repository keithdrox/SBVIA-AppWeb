package com.sbvia.backend.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UsernameGeneratorServiceTest {

    private final UsernameGeneratorService service = new UsernameGeneratorService();

    @Test
    void testNormalizar() {
        assertEquals("", service.normalizar(null));
        assertEquals("", service.normalizar(""));
        assertEquals("nino", service.normalizar("Niño"));
        assertEquals("jose perez", service.normalizar("José  Pérez "));
    }

    @Test
    void testGenerarBase() {
        // Base normal
        assertEquals("jcruzp", service.generarBase("Justyn Keith", "Cruz Perez"));
        // Base muy corta
        assertEquals("anli", service.generarBase("Ana", "Li"));
        // Base sin apellidos
        assertEquals("just", service.generarBase("Justyn", ""));
        assertEquals("user0", service.generarBase("", ""));
        // Particulas
        assertEquals("jdelacruzp", service.generarBase("Juan", "de la Cruz Perez"));
        assertEquals("mdelosantosp", service.generarBase("Maria", "de los Santos Perez"));
        // Base muy larga truncada
        String largoNombre = "Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        String largoBase = service.generarBase(largoNombre, largoNombre);
        assertEquals(50, largoBase.length());
    }

    @Test
    void testGenerarSiguienteDisponible() {
        assertEquals("jcruzp", service.generarSiguienteDisponible("jcruzp", null));
        assertEquals("jcruzp", service.generarSiguienteDisponible("jcruzp", List.of()));
        assertEquals("jcruzp1", service.generarSiguienteDisponible("jcruzp", List.of("jcruzp")));
        assertEquals("jcruzp2", service.generarSiguienteDisponible("jcruzp", List.of("jcruzp", "jcruzp1")));
        assertEquals("jcruzp", service.generarSiguienteDisponible("jcruzp", List.of("otro")));
        assertEquals("jcruzp3", service.generarSiguienteDisponible("jcruzp", List.of("jcruzp", "jcruzp2")));
    }
}
