package com.sbvia.backend.service;

import com.sbvia.backend.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de intentos de inicio de sesión (OWASP A07: Fallas de identificación
 * y autenticación). Cuenta los intentos fallidos por dirección IP dentro de una
 * ventana deslizante y, al superar el umbral, bloquea temporalmente nuevos intentos
 * con HTTP 429 (Too Many Requests).
 *
 * <p>Los umbrales se inyectan desde configuración externa con valores por defecto:
 * {@code security.login.max-attempts} (por defecto 5) y
 * {@code security.login.lock-duration-seconds} (por defecto 60 s).</p>
 */
@Service
public class LoginRateLimiter {

    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private static final long DEFAULT_LOCK_SECONDS = 60L;

    private final int maxAttempts;
    private final long lockSeconds;
    private final ConcurrentHashMap<String, Attempts> attemptsByIp = new ConcurrentHashMap<>();

    public LoginRateLimiter(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lock-duration-seconds:60}") long lockSeconds) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.lockSeconds = Math.max(1, lockSeconds);
    }

    /**
     * Verifica si la IP ya superó el número de intentos fallidos permitidos.
     * Lanza {@link RateLimitExceededException} (HTTP 429) cuando está bloqueada.
     */
    public void check(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        Attempts att = attemptsByIp.get(ip);
        if (att != null && att.failed >= maxAttempts) {
            long now = Instant.now().getEpochSecond();
            if (now - att.firstFailedAt < lockSeconds) {
                throw new RateLimitExceededException(
                        "Demasiados intentos fallidos. Intente de nuevo en unos segundos.");
            }
            attemptsByIp.remove(ip);
        }
    }

    /**
     * Registra un intento fallido para la IP. Implementa una ventana deslizante:
     * si el último fallo fue hace más de {@link #lockSeconds} segundos, se reinicia
     * el contador.
     */
    public void recordFailure(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        long now = Instant.now().getEpochSecond();
        attemptsByIp.compute(ip, (k, att) -> {
            if (att == null || now - att.firstFailedAt >= lockSeconds) {
                return new Attempts(now, 1);
            }
            att.failed++;
            return att;
        });
    }

    /**
     * Limpia el contador de la IP después de un inicio de sesión exitoso.
     */
    public void reset(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        attemptsByIp.remove(ip);
    }

    private static final class Attempts {
        final long firstFailedAt;
        int failed;

        Attempts(long firstFailedAt, int failed) {
            this.firstFailedAt = firstFailedAt;
            this.failed = failed;
        }
    }
}
