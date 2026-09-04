package com.sbvia.backend.service.retroalimentacion;

public class IaNoDisponibleException extends RuntimeException {

    public IaNoDisponibleException(String message) {
        super(message);
    }

    public IaNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
