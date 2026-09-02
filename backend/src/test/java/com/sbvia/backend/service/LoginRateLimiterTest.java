package com.sbvia.backend.service;

import com.sbvia.backend.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias del limitador de intentos de inicio de sesión (OWASP A07).
 * Con umbral por defecto de 5 intentos fallidos, el sexto debe lanzar
 * HTTP 429 (RateLimitExceededException).
 */
class LoginRateLimiterTest {

    @Test
    void permiteHastaCincoIntentosFallidosYLanza429EnElSexto() {
        LoginRateLimiter limiter = new LoginRateLimiter(5, 60);

        for (int i = 1; i <= 5; i++) {
            limiter.recordFailure("10.0.0.1");
        }

        assertThatThrownBy(() -> limiter.check("10.0.0.1"))
                .as("el sexto intento fallido debe devolver 429")
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void noBloqueaCuandoElNumeroDeIntentosEstaPorDebajoDelUmbral() {
        LoginRateLimiter limiter = new LoginRateLimiter(10, 60);

        limiter.recordFailure("10.0.0.2");
        limiter.recordFailure("10.0.0.2");

        assertThatCode(() -> limiter.check("10.0.0.2")).doesNotThrowAnyException();
    }

    @Test
    void resetLimpiaElContadorDeLaIp() {
        LoginRateLimiter limiter = new LoginRateLimiter(2, 60);

        limiter.recordFailure("10.0.0.3");
        limiter.recordFailure("10.0.0.3");
        limiter.reset("10.0.0.3");
        limiter.recordFailure("10.0.0.3");

        assertThatCode(() -> limiter.check("10.0.0.3")).doesNotThrowAnyException();
    }
}
