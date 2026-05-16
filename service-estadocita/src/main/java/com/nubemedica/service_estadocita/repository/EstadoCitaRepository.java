package com.nubemedica.service_estadocita.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_estadocita.model.EstadoCita;

@Repository
public interface EstadoCitaRepository extends JpaRepository<EstadoCita,Long>{

      
}
