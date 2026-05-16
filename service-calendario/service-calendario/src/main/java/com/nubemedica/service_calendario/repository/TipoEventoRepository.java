package com.nubemedica.service_calendario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_calendario.model.TipoEvento;

@Repository
public interface TipoEventoRepository extends JpaRepository<TipoEvento, Long> {

    TipoEvento findByNombreTipo(String nombreTipo);
    
}
