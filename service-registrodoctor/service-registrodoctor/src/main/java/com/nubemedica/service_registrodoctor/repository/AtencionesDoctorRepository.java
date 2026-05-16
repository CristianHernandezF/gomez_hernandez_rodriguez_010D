package com.nubemedica.service_registrodoctor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_registrodoctor.model.AtencionesDoctor;

@Repository
public interface AtencionesDoctorRepository extends JpaRepository<AtencionesDoctor, Long> {

    List<AtencionesDoctor> findByDoctorRunDoctor(String runDoctor);

    List<AtencionesDoctor> findByRunPaciente(String runPaciente);

    void deleteByRunPacienteAndDoctorRunDoctor(String runPaciente, String runDoctor);

    boolean existsByDoctorRunDoctorAndRunPaciente(String runDoctor, String runPaciente);

    @Query("SELECT DISTINCT a.runPaciente FROM AtencionesDoctor a WHERE a.doctor.runDoctor = :runDoctor")
    List<String> findRunPacientesByRunDoctor(@Param("runDoctor") String runDoctor);
}
