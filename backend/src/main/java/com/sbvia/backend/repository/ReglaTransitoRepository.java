package com.sbvia.backend.repository;

import com.sbvia.backend.entity.ReglaTransito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReglaTransitoRepository extends JpaRepository<ReglaTransito, Integer> {
    List<ReglaTransito> findAllByOrderByIdReglaTransitoDesc();
}
