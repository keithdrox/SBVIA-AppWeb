package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Usuario.
 * Extiende JpaRepository para CRUD genérico, paginación y ordenación.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por email (usado en autenticación).
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     */
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.query.Procedure(name = "Usuario.actualizarInactivos")
    Integer actualizarUsuariosInactivos(@org.springframework.data.repository.query.Param("p_fecha_limite") java.time.LocalDate pFechaLimite);
}
