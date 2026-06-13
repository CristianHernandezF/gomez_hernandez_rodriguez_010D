package com.nubemedica.service_telemedicina.repository;

import com.nubemedica.service_telemedicina.model.SesionTelemedicina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesionTelemedicinaRepository extends JpaRepository<SesionTelemedicina, Long> {


    
}
