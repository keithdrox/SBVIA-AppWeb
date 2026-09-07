package com.sbvia.backend.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio encargado de generar nombres de usuario deterministas, normalizados y únicos,
 * inspirados en el estándar SGA (Sistema de Gestión Académica) de la UTEQ:
 * inicial del primer nombre + primer apellido + inicial del segundo apellido (ej: jcruzp).
 *
 * Cumple con las restricciones de la base de datos:
 * - longitud mínima >= 4 (chk_usuario_nombre_usuario)
 * - longitud máxima <= 60 (VARCHAR(60) en usuario.nombre_usuario)
 * - caracteres alfanuméricos seguros para login
 */
@Service
public class UsernameGeneratorService {

    private static final Set<String> PARTICULAS_APELLIDO = Set.of(
            "de", "del", "la", "las", "los", "san", "santa"
    );

    /**
     * Normaliza un texto eliminando tildes, diacríticos (ej: ñ -> n),
     * caracteres especiales, espacios extras y convirtiendo a minúsculas.
     */
    public String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        // Descomposición canónica Unicode (NFD) para separar letras base de acentos/tildes
        String descompuesto = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        // Eliminar marcas diacríticas
        String sinDiacriticos = descompuesto.replaceAll("\\p{M}", "");
        // Conservar solo caracteres alfanuméricos y espacios
        String soloAlfanumerico = sinDiacriticos.replaceAll("[^a-zA-Z0-9\\s]", " ");
        // Reducir espacios múltiples y pasar a minúsculas
        return soloAlfanumerico.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Genera la base del nombre de usuario a partir de nombres y apellidos.
     * Formato UTEQ/SGA:
     * - Letra inicial del primer nombre
     * - Primer apellido (ignorando partículas como 'de', 'la', etc. o integrándolas si es necesario)
     * - Letra inicial del segundo apellido (si existe)
     *
     * Ejemplo:
     * "Justyn Keith", "Cruz Perez" -> "jcruzp"
     * "Ana", "Li"                  -> "anli" (expandido a >= 4 caracteres)
     * "José Ángel", "Muñoz"        -> "jmunoz"
     */
    public String generarBase(String nombres, String apellidos) {
        String normNombres = normalizar(nombres);
        String normApellidos = normalizar(apellidos);

        String[] tokensNombres = normNombres.isEmpty() ? new String[0] : normNombres.split(" ");
        String[] tokensApellidos = normApellidos.isEmpty() ? new String[0] : normApellidos.split(" ");

        String primerNombre = tokensNombres.length > 0 ? tokensNombres[0] : "user";
        String inicialNombre = primerNombre.substring(0, 1);

        // Procesar apellidos considerando partículas comunes en español
        List<String> apellidosLimpios = new ArrayList<>();
        for (int i = 0; i < tokensApellidos.length; i++) {
            String token = tokensApellidos[i];
            if (PARTICULAS_APELLIDO.contains(token) && (i + 1 < tokensApellidos.length)) {
                // Si viene 'de la cruz', une la partícula con el apellido para formar 'delacruz'
                // o conserva la raíz según los tokens restantes
                StringBuilder compuesto = new StringBuilder(token);
                while (i + 1 < tokensApellidos.length && PARTICULAS_APELLIDO.contains(tokensApellidos[i + 1])) {
                    i++;
                    compuesto.append(tokensApellidos[i]);
                }
                if (i + 1 < tokensApellidos.length) {
                    i++;
                    compuesto.append(tokensApellidos[i]);
                }
                apellidosLimpios.add(compuesto.toString());
            } else if (!token.isBlank()) {
                apellidosLimpios.add(token);
            }
        }

        String primerApellido;
        String inicialSegundoApellido = "";

        if (apellidosLimpios.isEmpty()) {
            primerApellido = primerNombre.length() >= 3 ? primerNombre : "usuario";
        } else {
            primerApellido = apellidosLimpios.get(0);
            if (apellidosLimpios.size() > 1) {
                String segundo = apellidosLimpios.get(1);
                if (!segundo.isEmpty()) {
                    inicialSegundoApellido = segundo.substring(0, 1);
                }
            }
        }

        String base = inicialNombre + primerApellido + inicialSegundoApellido;

        // Garantizar restricción de base de datos: longitud mínima >= 4 caracteres
        if (base.length() < 4) {
            // Intentar tomar más caracteres del primer nombre
            if (primerNombre.length() > 1) {
                int letrasFaltantes = 4 - base.length();
                int endIndex = Math.min(primerNombre.length(), 1 + letrasFaltantes);
                String prefijoExtendido = primerNombre.substring(0, endIndex);
                base = prefijoExtendido + primerApellido + inicialSegundoApellido;
            }
            // Si aún es menor a 4 (ej. nombre "A", apellido "Li"), rellenar de forma segura
            while (base.length() < 4) {
                base = base + "0";
            }
        }

        // Limitar la longitud de la base a 50 caracteres para reservar espacio a sufijos numéricos
        // (PostgreSQL tiene VARCHAR(60))
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }

        return base;
    }

    /**
     * Determina el siguiente nombre de usuario disponible dada una lista de existentes.
     * Si 'base' no existe, retorna 'base'.
     * Si ya existe 'base', genera 'base1', 'base2', etc.
     */
    public String generarSiguienteDisponible(String base, Collection<String> existentes) {
        if (existentes == null || existentes.isEmpty()) {
            return base;
        }

        Set<String> existentesSet = new HashSet<>();
        for (String exist : existentes) {
            if (exist != null) {
                existentesSet.add(exist.trim().toLowerCase(Locale.ROOT));
            }
        }

        String baseLower = base.toLowerCase(Locale.ROOT);
        if (!existentesSet.contains(baseLower)) {
            return baseLower;
        }

        // Buscar el sufijo numérico más alto disponible
        Pattern pattern = Pattern.compile("^" + Pattern.quote(baseLower) + "(\\d*)$");
        int maxNumero = 0;
        boolean baseSolaExiste = false;

        for (String exist : existentesSet) {
            Matcher matcher = pattern.matcher(exist);
            if (matcher.matches()) {
                String numStr = matcher.group(1);
                if (numStr.isEmpty()) {
                    baseSolaExiste = true;
                } else {
                    try {
                        int num = Integer.parseInt(numStr);
                        if (num > maxNumero) {
                            maxNumero = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        int siguiente = (maxNumero == 0 && baseSolaExiste) ? 1 : (maxNumero + 1);
        return baseLower + siguiente;
    }
}
