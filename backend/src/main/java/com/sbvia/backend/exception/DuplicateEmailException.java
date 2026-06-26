package com.sbvia.backend.exception;

/**
 * Excepción lanzada al intentar registrar un email que ya existe en la BD.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
