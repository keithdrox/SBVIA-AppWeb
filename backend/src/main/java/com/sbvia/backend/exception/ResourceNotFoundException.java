package com.sbvia.backend.exception;

/**
 * Excepción lanzada cuando un recurso (ej. usuario, escenario) no es encontrado en la BD.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
