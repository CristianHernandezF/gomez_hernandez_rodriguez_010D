package com.nubemedica.service_fichamedica.repository;

import com.nubemedica.service_fichamedica.model.TelefonoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefonoEmergenciaRepository extends JpaRepository<TelefonoEmergencia, Long> {
    
}