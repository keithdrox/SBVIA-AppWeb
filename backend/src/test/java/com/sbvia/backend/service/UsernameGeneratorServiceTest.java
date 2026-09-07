package com.sbvia.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameGeneratorServiceTest {

    private UsernameGeneratorService generator;

    @BeforeEach
    void setUp() {
        generator = new UsernameGeneratorService();
    }

    @Test
    @DisplayName("Genera jcruzp para Justyn Keith Cruz Perez")
    void generaParaJustynKeithCruzPerez() {
        String base = generator.generarBase("Justyn Keith", "Cruz Perez");
        assertThat(base).isEqualTo("jcruzp");
    }

    @Test
    @DisplayName("Normaliza tildes correctamente: José Ángel Muñoz -> jmunoz")
    void normalizaTildesYEnes() {
        String base = generator.generarBase("José Ángel", "Muñoz");
        assertThat(base).isEqualTo("jmunoz");
    }

    @Test
    @DisplayName("Normaliza Ñ en apellidos y nombres: Iñigo Peña -> ipena")
    void normalizaLetraEne() {
        String base = generator.generarBase("Iñigo", "Peña");
        assertThat(base).isEqualTo("ipena");
    }

    @Test
    @DisplayName("Maneja espacios múltiples y mayúsculas/minúsculas")
    void manejaEspaciosMultiples() {
        String base = generator.generarBase("  María   Elena  ", "  Ríos   Vargas  ");
        assertThat(base).isEqualTo("mriosv");
    }

    @Test
    @DisplayName("Garantiza longitud mínima de 4 caracteres para BD (chk_usuario_nombre_usuario)")
    void garantizaLongitudMinimaCuatro() {
        String base = generator.generarBase("Ana", "Li");
        assertThat(base.length()).isGreaterThanOrEqualTo(4);
        assertThat(base).isEqualTo("anli");
    }

    @Test
    @DisplayName("Maneja usuario con un solo apellido: Carlos Mendoza -> cmendoza")
    void manejaUnSoloApellido() {
        String base = generator.generarBase("Carlos", "Mendoza");
        assertThat(base).isEqualTo("cmendoza");
    }

    @Test
    @DisplayName("Maneja partículas en apellidos: Juan de la Cruz Perez")
    void manejaParticulasApellidos() {
        String base = generator.generarBase("Juan", "de la Cruz Perez");
        assertThat(base).isIn("jdelacruzp", "jcruzp");
    }

    @Test
    @DisplayName("Genera sufijos numéricos ante duplicados: base -> base1 -> base2")
    void generaSufijosNumericos() {
        String base = "jcruzp";

        // Primer usuario: no existe aún
        String u1 = generator.generarSiguienteDisponible(base, List.of());
        assertThat(u1).isEqualTo("jcruzp");

        // Segundo usuario: ya existe jcruzp -> jcruzp1
        String u2 = generator.generarSiguienteDisponible(base, List.of("jcruzp"));
        assertThat(u2).isEqualTo("jcruzp1");

        // Tercer usuario: ya existen jcruzp y jcruzp1 -> jcruzp2
        String u3 = generator.generarSiguienteDisponible(base, List.of("jcruzp", "jcruzp1"));
        assertThat(u3).isEqualTo("jcruzp2");

        // Salto de números: existen jcruzp, jcruzp1, jcruzp5 -> jcruzp6
        String u4 = generator.generarSiguienteDisponible(base, List.of("jcruzp", "jcruzp1", "jcruzp5"));
        assertThat(u4).isEqualTo("jcruzp6");
    }

    @Test
    @DisplayName("Garantiza que la longitud nunca exceda el límite de la BD (60 caracteres)")
    void garantizaLongitudMaxima() {
        String nombresLargos = "Esteban Constantino Maximiliano Hermenegildo";
        String apellidosLargos = "De La Santisima Trinidad Fernandez De Cordoba";
        String base = generator.generarBase(nombresLargos, apellidosLargos);
        assertThat(base.length()).isLessThanOrEqualTo(50);

        String conSufijo = generator.generarSiguienteDisponible(base, List.of(base));
        assertThat(conSufijo.length()).isLessThanOrEqualTo(60);
    }
}
