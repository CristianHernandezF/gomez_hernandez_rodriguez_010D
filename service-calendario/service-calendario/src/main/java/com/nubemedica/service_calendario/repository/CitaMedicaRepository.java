package com.nubemedica.service_calendario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import com.nubemedica.service_calendario.model.CitaMedica;

import jakarta.transaction.Transactional;

@Repository
public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {
    List<CitaMedica> findByRunDoctorAndRunPaciente(String runDoctor, String runPaciente);
    
    @Modifying
    @Transactional
    void deleteByRunDoctorAndRunPaciente(String runDoctor, String runPaciente);

    List<CitaMedica> findByRunDoctor(String runDoctor);
}