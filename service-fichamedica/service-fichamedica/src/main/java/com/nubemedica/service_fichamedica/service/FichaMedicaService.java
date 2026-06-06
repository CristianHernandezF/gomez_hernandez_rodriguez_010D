package com.nubemedica.service_fichamedica.service;

import com.nubemedica.service_fichamedica.dto.*;
import com.nubemedica.service_fichamedica.exceptions.ComunicacionMicroservicioException;
import com.nubemedica.service_fichamedica.exceptions.DatoDuplicadoException;
import com.nubemedica.service_fichamedica.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_fichamedica.model.ContactoProfesional;
import com.nubemedica.service_fichamedica.model.FarmacosRecetados;
import com.nubemedica.service_fichamedica.model.FichaMedica;
import com.nubemedica.service_fichamedica.model.TelefonoEmergencia;
import com.nubemedica.service_fichamedica.repository.FichaMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FichaMedicaService {

    @Autowired
    private FichaMedicaRepository fichaMedicaRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${microservicio.pacientes.url:http://localhost:8081/api/v1/pacientes}")
    private String urlMsPacientes;

    @Value("${ms.reportes.url}")
    private String urlMsReportes;

    // =====================================================================
    // CREAR FICHA MÉDICA
    // =====================================================================
    @Transactional
    public FichaMedicaResponse crearFicha(String runPaciente, String runDoctor) {
        if (fichaMedicaRepository.existsByRunPacienteAndRunDoctor(runPaciente, runDoctor)) {
            throw new DatoDuplicadoException(
                "Ya existe una ficha médica para el paciente " + runPaciente + " con el doctor " + runDoctor
            );
        }

        FichaMedica nuevaFicha = new FichaMedica();
        nuevaFicha.setRunPaciente(runPaciente);
        nuevaFicha.setRunDoctor(runDoctor);

        FichaMedica guardada = fichaMedicaRepository.save(nuevaFicha);
        PacienteDTO paciente = obtenerDatosPaciente(runPaciente);
        return mapearAResponse(guardada, paciente);
    }

    // =====================================================================
    // OBTENER FICHA POR ID
    // =====================================================================
    @Transactional(readOnly = true)
    public FichaMedicaResponse obtenerFichaPorId(Long idFicha) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha
            ));

        PacienteDTO paciente = obtenerDatosPaciente(ficha.getRunPaciente());
        return mapearAResponse(ficha, paciente);
    }

    // =====================================================================
    // OBTENER FICHA POR RUN PACIENTE + RUN DOCTOR
    // =====================================================================
    @Transactional(readOnly = true)
    public FichaMedicaResponse obtenerFichaPorPacienteYDoctor(String runPaciente, String runDoctor) {
        FichaMedica ficha = fichaMedicaRepository
            .findByRunPacienteAndRunDoctor(runPaciente, runDoctor)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe ficha para el paciente " + runPaciente + " con el doctor " + runDoctor
            ));

        PacienteDTO paciente = obtenerDatosPaciente(runPaciente);
        return mapearAResponse(ficha, paciente);
    }

    // =====================================================================
    // LISTAR TODAS LAS FICHAS DE UN DOCTOR
    // =====================================================================
    @Transactional(readOnly = true)
    public List<FichaMedicaResponse> listarFichasPorDoctor(String runDoctor) {
        return fichaMedicaRepository.findByRunDoctor(runDoctor).stream()
            .map(ficha -> {
                PacienteDTO paciente = obtenerDatosPaciente(ficha.getRunPaciente());
                return mapearAResponse(ficha, paciente);
            })
            .collect(Collectors.toList());
    }

    // =====================================================================
    // ACTUALIZAR FICHA
    // =====================================================================
    @Transactional
    public FichaMedicaResponse actualizarFicha(Long idFicha, FichaMedicaUpdateRequest request) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha
            ));

        ficha.setHistorialFamiliar(request.getHistorialFamiliar());
        ficha.setDiagnostico(request.getHipotesisDiagnostica());

        // Reemplazar contactos profesionales
        if (request.getContactosProfesionales() != null) {
            ficha.getContactoPro().clear();
            request.getContactosProfesionales().forEach(dto -> {
                ContactoProfesional cp = new ContactoProfesional();
                cp.setNombres(dto.getNombres());
                cp.setApellidos(dto.getApellidos());
                cp.setCorreo(dto.getCorreo());
                cp.setFichaMedica(ficha);
                ficha.getContactoPro().add(cp);
            });
        }

        // Reemplazar teléfonos de emergencia — solo numTelefono y descripcion
        if (request.getTelefonosEmergencia() != null) {
            ficha.getTelefonos().clear();
            request.getTelefonosEmergencia().forEach(dto -> {
                TelefonoEmergencia te = new TelefonoEmergencia();
                te.setNumTelefono(dto.getNumTelefono());
                te.setDescripcion(dto.getDescripcion());
                te.setFichaMedica(ficha);
                ficha.getTelefonos().add(te);
            });
        }

        // Reemplazar fármacos recetados
        if (request.getFarmacos() != null) {
            ficha.getFarmacos().clear();
            request.getFarmacos().forEach(dto -> {
                FarmacosRecetados fr = new FarmacosRecetados();
                fr.setNombreFarmaco(dto.getNombreFarmaco());
                fr.setDosis(dto.getDosis());
                fr.setFichaMedica(ficha);
                ficha.getFarmacos().add(fr);
            });
        }

        FichaMedica actualizada = fichaMedicaRepository.save(ficha);
        PacienteDTO paciente = obtenerDatosPaciente(actualizada.getRunPaciente());
        return mapearAResponse(actualizada, paciente);
    }

    // =====================================================================
    // ELIMINAR FICHA
    // =====================================================================
    @Transactional
    public void eliminarFicha(Long idFicha) {
        if (!fichaMedicaRepository.existsById(idFicha)) {
            throw new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha
            );
        }
        fichaMedicaRepository.deleteById(idFicha);
    }

    // =====================================================================
    // MÉTODOS PRIVADOS
    // =====================================================================

    private PacienteDTO obtenerDatosPaciente(String runPaciente) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(urlMsPacientes + "/{run}", runPaciente)
                .retrieve()
                .bodyToMono(PacienteDTO.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException(
                "No se encontró el paciente con RUN: " + runPaciente
            );
        } catch (Exception e) {
            // Se pasa e como causa (Throwable) — el constructor requiere (String, Throwable)
            throw new ComunicacionMicroservicioException(
                "Error al comunicarse con el microservicio de pacientes: " + e.getMessage(), e
            );
        }
    }

    private FichaMedicaResponse mapearAResponse(FichaMedica ficha, PacienteDTO paciente) {

        List<com.nubemedica.service_fichamedica.dto.ContactoProfesional> contactosDto =
            ficha.getContactoPro() != null
                ? ficha.getContactoPro().stream().map(cp -> {
                    com.nubemedica.service_fichamedica.dto.ContactoProfesional dto =
                        new com.nubemedica.service_fichamedica.dto.ContactoProfesional();
                    dto.setNombres(cp.getNombres());
                    dto.setApellidos(cp.getApellidos());
                    dto.setCorreo(cp.getCorreo());
                    return dto;
                }).collect(Collectors.toList())
                : new ArrayList<>();

        List<com.nubemedica.service_fichamedica.dto.TelefonoEmergencia> telefonosDto =
            ficha.getTelefonos() != null
                ? ficha.getTelefonos().stream().map(te -> {
                    com.nubemedica.service_fichamedica.dto.TelefonoEmergencia dto =
                        new com.nubemedica.service_fichamedica.dto.TelefonoEmergencia();
                    dto.setNumTelefono(te.getNumTelefono());
                    dto.setDescripcion(te.getDescripcion());
                    return dto;
                }).collect(Collectors.toList())
                : new ArrayList<>();

        List<com.nubemedica.service_fichamedica.dto.FarmacosRecetados> farmacosDto =
            ficha.getFarmacos() != null
                ? ficha.getFarmacos().stream().map(fr -> {
                    com.nubemedica.service_fichamedica.dto.FarmacosRecetados dto =
                        new com.nubemedica.service_fichamedica.dto.FarmacosRecetados();
                    dto.setNombreFarmaco(fr.getNombreFarmaco());
                    dto.setDosis(fr.getDosis());
                    return dto;
                }).collect(Collectors.toList())
                : new ArrayList<>();

        return FichaMedicaResponse.builder()
            .idFichaMedica(ficha.getIdFichaMedica())
            .runPaciente(ficha.getRunPaciente())
            .runDoctor(ficha.getRunDoctor())
            .historialFamiliar(ficha.getHistorialFamiliar())
            .hipotesisDiagnostica(ficha.getDiagnostico())
            .datosPaciente(paciente)
            .contactos(contactosDto)
            .telefonos(telefonosDto)
            .farmacos(farmacosDto)
            .build();
    }

    // =====================================================================
    // AGREGAR REPORTE A UNA FICHA (llama a ms-reportes via WebClient)
    // =====================================================================
    @Transactional
    public ReporteDTO agregarReporte(Long idFicha, ReporteCreateRequest request) {

        if (!fichaMedicaRepository.existsById(idFicha))
            throw new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha);

        Map<String, Object> body = new HashMap<>();
        body.put("nombreReporte", request.getNombreReporte());
        body.put("idFichaMedica", idFicha);
        body.put("descripcion", request.getDescripcion());

        try {
            return webClientBuilder.build()
                .post()
                .uri(urlMsReportes)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ReporteDTO.class)
                .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                "Error al crear reporte en ms-reportes: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // OBTENER REPORTES DE UNA FICHA (llamado internamente desde mapearAResponse)
    // =====================================================================
    private List<ReporteDTO> obtenerReportesDeFicha(Long idFicha) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(urlMsReportes + "/ficha/" + idFicha)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReporteDTO>>() {})
                .block();
        } catch (Exception e) {
            // Si ms-reportes no responde, devuelve lista vacía
            // para no romper la consulta de la ficha
            return Collections.emptyList();
        }
    }
}