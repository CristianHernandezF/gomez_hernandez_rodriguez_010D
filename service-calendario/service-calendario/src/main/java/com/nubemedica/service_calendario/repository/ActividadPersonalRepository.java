package com.nubemedica.service_calendario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_calendario.model.ActividadPersonal;

@Repository
public interface ActividadPersonalRepository extends JpaRepository<ActividadPersonal, Long>{

    List<ActividadPersonal> findByRunDoctor(String runDoctor);

}
