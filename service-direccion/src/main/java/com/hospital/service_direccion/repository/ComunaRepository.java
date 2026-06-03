package com.hospital.service_direccion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.service_direccion.model.Comuna;
import com.hospital.service_direccion.model.Region;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna,Long>{

    Optional<Comuna> findByNombreAndRegion(String nombre, Region region);

}
