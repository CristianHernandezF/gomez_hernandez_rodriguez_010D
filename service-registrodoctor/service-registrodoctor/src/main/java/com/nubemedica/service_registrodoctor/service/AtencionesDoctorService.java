package com.nubemedica.service_registrodoctor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registrodoctor.dto.*;
import com.nubemedica.service_registrodoctor.exceptions.ComunicacionMicroservicioException;
import com.nubemedica.service_registrodoctor.model.AtencionesDoctor;
import com.nubemedica.service_registrodoctor.model.RegistroDoctor;
import com.nubemedica.service_registrodoctor.repository.AtencionesDoctorRepository;
import com.nubemedica.service_registrodoctor.repository.RegistroDoctorRepository;

import jakarta.transaction.Transactional;

@Service
public class AtencionesDoctorService {

    @Autowired
    private RegistroDoctorRepository doctorRepository;

    @Autowired
    private AtencionesDoctorRepository atencionesRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Transactional
    public AtencionesDoctor registrarAtencion(AtencionesDoctorDTO atencionDTO) {
        AtencionesDoctor atencion = new AtencionesDoctor();
        atencion.setRunPaciente(atencionDTO.getRunPaciente());
        atencion.setDoctor(doctorRepository.findByRunDoctor(atencionDTO.getRunDoctor()));
        return atencionesRepository.save(atencion);
    }

    public List<AtencionesDoctor> listarPacientesDeDoctor(String runDoctor) {
        List<AtencionesDoctor> atenciones = atencionesRepository.findByDoctorRunDoctor(runDoctor);

        return atenciones.stream()
                .map(this::enriquecerConDatosPaciente)
                .map(atencion -> {
                    atencion.setDoctor(darleLaDireccion(atencion.getDoctor()));
                    return atencion;
                }).toList();
    }

    public List<String> RutsPacientesDoctor(String runDoctor) {
        return atencionesRepository.findRunPacientesByRunDoctor(runDoctor);
    }

    public List<AtencionesDoctor> listarDoctoresDePaciente(String runPaciente) {
        return atencionesRepository.findByRunPaciente(runPaciente);
    }

    @Modifying
    @Transactional
    public void eliminarRelacion(String runPaciente, String runDoctor) {
        atencionesRepository.deleteByRunPacienteAndDoctorRunDoctor(runPaciente, runDoctor);
        eliminarCitasEnCalendarioMS(runDoctor, runPaciente);
    }

    public boolean verificarRelacion(String runDoctor, String runPaciente) {
        return atencionesRepository.existsByDoctorRunDoctorAndRunPaciente(runDoctor, runPaciente);
    }

    private AtencionesDoctor enriquecerConDatosPaciente(AtencionesDoctor atencion) {
        try {
            PacienteResumen resumen = webClientBuilder.build().get()
                    .uri("http://localhost:8084/api/v1/pacientes/" + atencion.getRunPaciente() + "/resumen")
                    .retrieve()
                    .bodyToMono(PacienteResumen.class)
                    .block();
            atencion.setDatosPaciente(resumen);
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al obtener resumen del paciente", e);
        }
        return atencion;
    }

    private RegistroDoctor darleLaDireccion(RegistroDoctor doctor) {
        if (doctor.getIdDireccion() != null) {
            try {
                // USAMOS EL DTO PLANO DireccionResponse
                DireccionResponse dir = webClientBuilder.build().get()
                        .uri("http://localhost:8083/api/v1/direcciones/" + doctor.getIdDireccion())
                        .retrieve()
                        .bodyToMono(DireccionResponse.class)
                        .block();
                doctor.setDatosDireccion(dir);
            } catch (Exception e) {
                doctor.setDatosDireccion(null);
            }
        }
        return doctor;
    }

    private void eliminarCitasEnCalendarioMS(String runDoctor, String runPaciente) {
        try {
            webClientBuilder.build().delete()
                    .uri("http://localhost:8086/api/v1/calendario/citas/doctor/" + runDoctor + "/paciente/" + runPaciente)
                    .retrieve().bodyToMono(Void.class).block();
        } catch (Exception e) {
            System.err.println("Error eliminando citas en calendario: " + e.getMessage());
        }
    }
}