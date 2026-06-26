package com.sbvia.backend.entity;

/**
 * Roles del sistema SBVIA.
 * Se almacenan como VARCHAR(20) en PostgreSQL con CHECK constraint.
 */
public enum Rol {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_INSTRUCTOR
}
