package com.nubemedica.service_fichamedica.repository;

import com.nubemedica.service_fichamedica.model.FichaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaMedicaRepository extends JpaRepository<FichaMedica, Long> {

    // Crucial para el flujo: Busca la ficha única de un paciente atendido por un doctor específico
    Optional<FichaMedica> findByRunPacienteAndRunDoctor(String runPaciente, String runDoctor);

    // Permite al doctor ver el listado de todas las fichas clínicas que él ha generado
    List<FichaMedica> findByRunDoctor(String runDoctor);

    // Verifica si ya existe una ficha para evitar duplicados en la inicialización
    boolean existsByRunPacienteAndRunDoctor(String runPaciente, String runDoctor);
}