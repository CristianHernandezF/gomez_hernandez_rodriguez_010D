package com.nubemedica.service_fichamedica.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nubemedica.service_fichamedica.dto.ContactoProfesionalDTO;
import com.nubemedica.service_fichamedica.dto.FarmacosRecetadosDTO;
import com.nubemedica.service_fichamedica.dto.FichaMedicaResponse;
import com.nubemedica.service_fichamedica.dto.FichaMedicaUpdateRequest;
import com.nubemedica.service_fichamedica.dto.PacienteDTO;
import com.nubemedica.service_fichamedica.dto.ReporteCreateRequest;
import com.nubemedica.service_fichamedica.dto.ReporteDTO;
import com.nubemedica.service_fichamedica.dto.ReporteUpdateRequest;
import com.nubemedica.service_fichamedica.dto.TelefonoEmergenciaDTO;
import com.nubemedica.service_fichamedica.exceptions.AccesoDenegadoException;
import com.nubemedica.service_fichamedica.exceptions.ComunicacionMicroservicioException;
import com.nubemedica.service_fichamedica.exceptions.DatoDuplicadoException;
import com.nubemedica.service_fichamedica.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_fichamedica.model.ContactoProfesional;
import com.nubemedica.service_fichamedica.model.FarmacosRecetados;
import com.nubemedica.service_fichamedica.model.FichaMedica;
import com.nubemedica.service_fichamedica.model.TelefonoEmergencia;
import com.nubemedica.service_fichamedica.repository.FichaMedicaRepository;

@Service
public class FichaMedicaService {

    @Autowired
    private FichaMedicaRepository fichaMedicaRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${microservicio.pacientes.url:http://localhost:8081/api/v1/pacientes}")
    private String urlMsPacientes;

    @Value("${ms.reportes.url:http://localhost:8088/api/v1/reportes}")
    private String urlMsReportes;

    @Value("${ms.doctores.url:http://localhost:8085/api/v1/atenciones}")
    private String urlMsDoctores;

    // =====================================================================
    // CREAR FICHA MÉDICA
    // =====================================================================
    @Transactional
    public FichaMedicaResponse crearFicha(String runPaciente, String runDoctorToken) {
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);

        if (fichaMedicaRepository.existsByRunPacienteAndRunDoctor(runPaciente, runDoctorToken)) {
            throw new DatoDuplicadoException(
                "Ya existe una ficha médica para el paciente " + runPaciente +
                " con el doctor " + runDoctorToken
            );
        }

        FichaMedica nuevaFicha = new FichaMedica();
        nuevaFicha.setRunPaciente(runPaciente);
        nuevaFicha.setRunDoctor(runDoctorToken);

        FichaMedica guardada = fichaMedicaRepository.save(nuevaFicha);
        PacienteDTO paciente = obtenerDatosPaciente(runPaciente);
        return mapearAResponse(guardada, paciente);
    }

    // CREAR FICHA INTERNA (llamada desde service-registropacientes)
    // Sin validación de relación — la relación acaba de ser creada
    // Si ya existe la ficha para este par, ignora 
    @Transactional
    public void crearFichaInterno(String runPaciente, String runDoctor) {
        if (fichaMedicaRepository.existsByRunPacienteAndRunDoctor(runPaciente, runDoctor)) {
            return; // Ya existe, no hace nada
        }

        FichaMedica nuevaFicha = new FichaMedica();
        nuevaFicha.setRunPaciente(runPaciente);
        nuevaFicha.setRunDoctor(runDoctor);
        fichaMedicaRepository.save(nuevaFicha);
    }


    // OBTENER FICHA POR ID
    @Transactional(readOnly = true)
    public FichaMedicaResponse obtenerFichaPorId(Long idFicha, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha
            ));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

        PacienteDTO paciente = obtenerDatosPaciente(ficha.getRunPaciente());
        return mapearAResponse(ficha, paciente);
    }

    // OBTENER FICHA POR RUN PACIENTE (del token se obtiene el doctor)
    @Transactional(readOnly = true)
    public FichaMedicaResponse obtenerFichaPorPacienteYDoctor(String runPaciente, String runDoctorToken) {
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);

        FichaMedica ficha = fichaMedicaRepository
            .findByRunPacienteAndRunDoctor(runPaciente, runDoctorToken)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe ficha para el paciente " + runPaciente +
                " con el doctor " + runDoctorToken
            ));

        PacienteDTO paciente = obtenerDatosPaciente(runPaciente);
        return mapearAResponse(ficha, paciente);
    }

    // =====================================================================
    // LISTAR TODAS LAS FICHAS DEL DOCTOR AUTENTICADO
    // =====================================================================
    @Transactional(readOnly = true)
    public List<FichaMedicaResponse> listarFichasPorDoctor(String runDoctorToken) {
        return fichaMedicaRepository.findByRunDoctor(runDoctorToken).stream()
            .map(ficha -> {
                PacienteDTO paciente = obtenerDatosPaciente(ficha.getRunPaciente());
                return mapearAResponse(ficha, paciente);
            }).collect(Collectors.toList());
    }

    // ACTUALIZAR FICHA
    @Transactional
    public FichaMedicaResponse actualizarFicha(Long idFicha, FichaMedicaUpdateRequest request, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha
            ));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

        ficha.setHistorialFamiliar(request.getHistorialFamiliar());
        ficha.setDiagnostico(request.getHipotesisDiagnostica());

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
    public void eliminarFicha(Long idFicha, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha
            ));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

        fichaMedicaRepository.deleteById(idFicha);
    }

    // =====================================================================
    // AGREGAR REPORTE A UNA FICHA
    // =====================================================================
    @Transactional
    public ReporteDTO agregarReporte(Long idFicha, ReporteCreateRequest request, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

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
    // OBTENER REPORTE ESPECÍFICO DE UNA FICHA
    // =====================================================================
    @Transactional(readOnly = true)
    public ReporteDTO obtenerReporteDeFicha(Long idFicha, Long idReporte, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

        return obtenerReportePorId(idReporte);
    }

    // =====================================================================
    // EDITAR REPORTE DE UNA FICHA
    // =====================================================================
    @Transactional
    public ReporteDTO editarReporteDeFicha(Long idFicha, Long idReporte, ReporteUpdateRequest request, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

        Map<String, Object> body = new HashMap<>();
        body.put("nombreReporte", request.getNombreReporte());
        body.put("idFichaMedica", idFicha);
        body.put("descripcion", request.getDescripcion());

        return actualizarReporteRemoto(idReporte, body);
    }

    // =====================================================================
    // ELIMINAR REPORTE DE UNA FICHA
    // =====================================================================
    @Transactional
    public void eliminarReporteDeFicha(Long idFicha, Long idReporte, String runDoctorToken) {
        FichaMedica ficha = fichaMedicaRepository.findById(idFicha)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró la ficha médica con ID: " + idFicha));

        validarRelacionDoctorPaciente(runDoctorToken, ficha.getRunPaciente());

        eliminarReporteRemoto(idReporte);
    }

    // =====================================================================
    // MÉTODOS PRIVADOS — SEGURIDAD
    // =====================================================================

    private void validarRelacionDoctorPaciente(String runDoctor, String runPaciente) {
        try {
            Boolean existe = webClientBuilder.build()
                .get()
                .uri(urlMsDoctores + "/existe/{runDoctor}/{runPaciente}", runDoctor, runPaciente)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

            if (Boolean.FALSE.equals(existe)) {
                throw new AccesoDenegadoException(
                    "Acceso denegado: No tiene relación de atención con el paciente " + runPaciente + "."
                );
            }
        } catch (AccesoDenegadoException e) {
            throw e;
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                "Error de validación en MS-DOCTORES: " + e.getMessage(), e
            );
        }
    }

    // =====================================================================
    // MÉTODOS PRIVADOS — COMUNICACIÓN CON MICROSERVICIOS
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
            throw new ComunicacionMicroservicioException(
                "Error al comunicarse con el microservicio de pacientes: " + e.getMessage(), e
            );
        }
    }

    private List<ReporteDTO> obtenerReportesDeFicha(Long idFicha) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(urlMsReportes + "/ficha/" + idFicha)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReporteDTO>>() {})
                .block();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private ReporteDTO obtenerReportePorId(Long idReporte) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(urlMsReportes + "/{idReporte}", idReporte)
                .retrieve()
                .bodyToMono(ReporteDTO.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException(
                "No se encontró el reporte con ID: " + idReporte
            );
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                "Error al obtener reporte desde ms-reportes: " + e.getMessage(), e);
        }
    }

    private ReporteDTO actualizarReporteRemoto(Long idReporte, Map<String, Object> body) {
        try {
            return webClientBuilder.build()
                .put()
                .uri(urlMsReportes + "/{idReporte}", idReporte)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ReporteDTO.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException(
                "No se encontró el reporte con ID: " + idReporte
            );
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                "Error al editar reporte en ms-reportes: " + e.getMessage(), e);
        }
    }

    private void eliminarReporteRemoto(Long idReporte) {
        try {
            webClientBuilder.build()
                .delete()
                .uri(urlMsReportes + "/{idReporte}", idReporte)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException(
                "No se encontró el reporte con ID: " + idReporte
            );
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                "Error al eliminar reporte en ms-reportes: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // MÉTODO PRIVADO — MAPEO
    // =====================================================================

    private FichaMedicaResponse mapearAResponse(FichaMedica ficha, PacienteDTO paciente) {

        List<ContactoProfesionalDTO> contactosDto = ficha.getContactoPro() != null
            ? ficha.getContactoPro().stream().map(cp -> {
                ContactoProfesionalDTO dto = new ContactoProfesionalDTO();
                dto.setNombres(cp.getNombres());
                dto.setApellidos(cp.getApellidos());
                dto.setCorreo(cp.getCorreo());
                return dto;
            }).collect(Collectors.toList())
            : new ArrayList<>();

        List<TelefonoEmergenciaDTO> telefonosDto = ficha.getTelefonos() != null
            ? ficha.getTelefonos().stream().map(te -> {
                TelefonoEmergenciaDTO dto = new TelefonoEmergenciaDTO();
                dto.setNumTelefono(te.getNumTelefono());
                dto.setDescripcion(te.getDescripcion());
                return dto;
            }).collect(Collectors.toList())
            : new ArrayList<>();

        List<FarmacosRecetadosDTO> farmacosDto = ficha.getFarmacos() != null
            ? ficha.getFarmacos().stream().map(fr -> {
                FarmacosRecetadosDTO dto = new FarmacosRecetadosDTO();
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
            .reportes(obtenerReportesDeFicha(ficha.getIdFichaMedica()))
            .build();
    }
}