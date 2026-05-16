package com.nubemedica.service_registrodoctor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registrodoctor.dto.AtencionesDoctorDTO;
import com.nubemedica.service_registrodoctor.dto.PacienteResumenDTO;
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

    // Método principal para listar pacientes de un doctor
    public List<AtencionesDoctor> listarPacientesDeDoctor(String runDoctor) {
        List<AtencionesDoctor> atenciones = atencionesRepository.findByDoctorRunDoctor(runDoctor);

        // Recorremos la lista y enriquecemos cada atención con los datos del paciente
        return atenciones.stream()
                .map(this::enriquecerConDatosPaciente).map(atencion -> {
                    // También enriquecemos el doctor con su dirección
                    atencion.setDoctor(darleLaDireccion(atencion.getDoctor()));
                    return atencion;}).toList();
    }

    public List<String> RutsPacientesDoctor(String runDoctor){
        return atencionesRepository.findRunPacientesByRunDoctor(runDoctor);
    }    

    public List<AtencionesDoctor> listarDoctoresDePaciente(String runPaciente) {
        return atencionesRepository.findByRunPaciente(runPaciente);
    }
    
    @Modifying
    @Transactional
    public void eliminarRelacion(String runPaciente, String runDoctor) {
        // 1. Borrar la relación física en la tabla atenciones_doctor
        atencionesRepository.deleteByRunPacienteAndDoctorRunDoctor(runPaciente, runDoctor);

        // 2. Notificar a MS-CALENDARIO para limpiar las citas de esa relación
        eliminarCitasEnCalendarioMS(runDoctor, runPaciente);
    }

    public boolean verificarRelacion(String runDoctor, String runPaciente){
        return atencionesRepository.existsByDoctorRunDoctorAndRunPaciente(runDoctor, runPaciente);
    }


    private AtencionesDoctor enriquecerConDatosPaciente(AtencionesDoctor atencion) {
        try {
            PacienteResumenDTO resumen = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8084/api/v1/pacientes/" + atencion.getRunPaciente() + "/resumen")
                    .retrieve()
                    .bodyToMono(PacienteResumenDTO.class)
                    .block();

            atencion.setDatosPaciente(resumen);
        } catch (Exception e) {
           throw new ComunicacionMicroservicioException("Error al comunicarse con MS-PACIENTES para obtener el al paciente", e);
        }
        return atencion;
    }

    private RegistroDoctor darleLaDireccion(RegistroDoctor doctor) {
        if (doctor.getIdDireccion() != null) {
            try {
                Object datosDireccion = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8083/api/v1/direcciones/" + doctor.getIdDireccion())
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block();
                doctor.setDatosDireccion(datosDireccion);
            } catch (Exception e) {
                throw new ComunicacionMicroservicioException("No se pudo obtener la dirección desde MS-DIRECCION", e);
            }
        }
        return doctor;
    }

    private void eliminarCitasEnCalendarioMS(String runDoctor, String runPaciente) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri("http://localhost:8086/api/v1/calendario/citas/doctor/" + runDoctor + "/paciente/" + runPaciente)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Logueamos el error pero permitimos que la desasociación continúe
            System.err.println("Error al limpiar citas en MS-CALENDARIO: " + e.getMessage());
        }
    }

}
