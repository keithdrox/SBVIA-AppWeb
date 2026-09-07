package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByCorreoIgnoreCaseOrNombreUsuarioIgnoreCase(String correo, String nombreUsuario);

    @Query("SELECT u.nombreUsuario FROM Usuario u WHERE LOWER(u.nombreUsuario) = LOWER(:base) OR LOWER(u.nombreUsuario) LIKE LOWER(CONCAT(:base, '%'))")
    List<String> findNombresUsuarioSimilares(@Param("base") String base);
}
