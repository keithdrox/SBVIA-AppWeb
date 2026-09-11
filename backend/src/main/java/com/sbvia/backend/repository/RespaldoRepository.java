package com.sbvia.backend.repository;

import com.sbvia.backend.model.Respaldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespaldoRepository extends JpaRepository<Respaldo, Long> {
    List<Respaldo> findAllByOrderByFechaInicioDesc();
}
