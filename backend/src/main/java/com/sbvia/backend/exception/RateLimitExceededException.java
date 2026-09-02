package com.sbvia.backend.exception;

/**
 * Excepción lanzada cuando un cliente supera el número permitido de intentos
 * de inicio de sesión (OWASP A07). Se traduce a HTTP 429 Too Many Requests
 * mediante {@link GlobalExceptionHandler}.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
