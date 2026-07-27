package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Rol.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Busca un rol por nombre.
     */
    Optional<Rol> findByNombre(String nombre);
}
