package com.nubemedica.service_registropacientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_registropacientes.dto.PacienteResumenDTO;
import com.nubemedica.service_registropacientes.model.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String>{

    List<Paciente> findByRunPacienteIn(List<String> runs);

    boolean existsByCorreo (String correo);

    boolean existsByNumTelefono (String numTelefono);

    @Query("SELECT new com.nubemedica.service_registropacientes.dto.PacienteResumenDTO(" +
           "p.runPaciente, " +
           "CONCAT(p.priNombre, ' ', COALESCE(p.segNombre, '')), " +
           "CONCAT(p.apaPaterno, ' ', p.apaMaterno)) " +
           "FROM Paciente p WHERE p.runPaciente = :run")
    Optional<PacienteResumenDTO> findResumenByRun(@Param("run") String run);

}
