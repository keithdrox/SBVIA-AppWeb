package com.sbvia.backend.repository;

import com.sbvia.backend.entity.ReglaTransito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReglaTransitoRepository extends JpaRepository<ReglaTransito, Integer> {

    List<ReglaTransito> findByActivaTrue();

    Optional<ReglaTransito> findByCodigo(String codigo);
}
