package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Usuario.
 * Extiende JpaRepository para CRUD genérico, paginación y ordenación.
 * Los métodos custom se derivan del nombre del método (Spring Data query derivation).
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email (usado en autenticación).
     * Genera: SELECT * FROM usuarios WHERE email = ?
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     * Usado para validar registro duplicado.
     */
    boolean existsByEmail(String email);
}
