package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    Optional<Vehiculo> findFirstByActivoTrueOrderByIdVehiculoAsc();
}
