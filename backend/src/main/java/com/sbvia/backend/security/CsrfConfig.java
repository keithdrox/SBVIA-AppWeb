package com.sbvia.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Repositorio CSRF compartido entre la configuración de seguridad y el filtro
 * de emisión, para que el token emitido sea el mismo que se valida.
 * Vive en su propia clase para evitar una referencia circular con
 * SecurityConfig. Envuelto para ignorar el borrado por rotación (ver
 * StatelessCsrfTokenRepository y ADR-009).
 */
@Configuration
public class CsrfConfig {

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new StatelessCsrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}
