package com.nubemedica.service_estadocita.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_estadocita.model.TipoEstadoCita;

@Repository
public interface TipoEstadoCitaRepository extends JpaRepository<TipoEstadoCita,Long> {

    
    TipoEstadoCita findByNombreEstado(String nombreEstado);

}
