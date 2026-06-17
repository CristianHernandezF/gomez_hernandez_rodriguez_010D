package com.nubemedica.service_calendario.controller;

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.service.ActividadPersonalService;
import com.nubemedica.service_calendario.service.CitaMedicaService;
import com.nubemedica.service_calendario.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendario")
@CrossOrigin(origins = "http://localhost:8081")
public class CalendarioController {

    @Autowired
    private CitaMedicaService citaMedicaService;

    @Autowired
    private ActividadPersonalService actividadService;

    @Autowired
    private EventoService eventoService;

    // ==========================================
    // ENDPOINTS: CITAS MÉDICAS
    // ==========================================

    @PostMapping("/citas")
    public ResponseEntity<CitaMedicaResponseDTO> crearCita(
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(citaMedicaService.crearCitaMedica(request, runDoctor, token), HttpStatus.CREATED);
    }

    @GetMapping("/citas/{id}")
    public ResponseEntity<CitaMedicaResponseDTO> obtenerCita(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(citaMedicaService.obtenerCitaMedicaPorId(id, runDoctor));
    }

    @GetMapping("/citas/mias")
    public ResponseEntity<List<CitaMedicaResponseDTO>> listarCitasPorDoctor(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(citaMedicaService.listarCitasMedicasPorDoctor(runDoctor));
    }

    @PutMapping("/citas/{id}")
    public ResponseEntity<CitaMedicaResponseDTO> actualizarCita(
            @PathVariable Long id, 
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(citaMedicaService.actualizarCitaMedica(id, request, runDoctor, token));
    }

    @DeleteMapping("/citas/{id}")
    public ResponseEntity<Void> eliminarCita(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        citaMedicaService.eliminarCitaMedica(id, runDoctor, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/citas/doctor/{runDoctor}/paciente/{runPaciente}")
    public ResponseEntity<Void> eliminarCitasRelacion(
            @PathVariable String runDoctor, 
            @PathVariable String runPaciente) {
        citaMedicaService.eliminarCitasEntreDoctorYPaciente(runDoctor, runPaciente);
        return ResponseEntity.noContent().build();
    }


    // ==========================================
    // ENDPOINTS: ACTIVIDADES PERSONALES
    // ==========================================

    @PostMapping("/actividades")
    public ResponseEntity<ActividadPersonalResponseDTO> crearActividad( 
            @Valid @RequestBody ActividadPersonalRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(actividadService.crearActividadPersonal(request, runDoctor, token), HttpStatus.CREATED);
    }

    @GetMapping("/actividades/{id}")
    public ResponseEntity<ActividadPersonalResponseDTO> obtenerActividad(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(actividadService.obtenerActividadPersonalPorId(id, runDoctor));
    }

    @GetMapping("/actividades/mias")
    public ResponseEntity<List<ActividadPersonalResponseDTO>> listarMisActividades(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(actividadService.listarActividadesPorDoctor(runDoctor));
    }

    @PutMapping("/actividades/{id}")
    public ResponseEntity<ActividadPersonalResponseDTO> actualizarActividad(
            @PathVariable Long id, 
            @Valid @RequestBody ActividadPersonalRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(actividadService.actualizarActividadPersonal(id, request, runDoctor, token));
    }

    @DeleteMapping("/actividades/{id}")
    public ResponseEntity<Void> eliminarActividad(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        actividadService.eliminarActividadPersonal(id, runDoctor);
        return ResponseEntity.noContent().build();
    }


    // ==========================================
    // ENDPOINTS: AGENDA GENERAL Y EVENTOS
    // ==========================================

    @GetMapping("/agenda/doctor")
    public ResponseEntity<List<EventoDTO>> obtenerAgendaCompleta(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        // Este endpoint devuelve Citas y Actividades mezcladas en orden cronológico
        return ResponseEntity.ok(eventoService.obtenerAgendaDoctor(runDoctor));
    }

    @DeleteMapping("/agenda/doctor/{runDoctor}")
    public ResponseEntity<Void> limpiarAgendaDoctor(@PathVariable String runDoctor) {
        eventoService.eliminarTodaLaAgendaDelDoctor(runDoctor);
        return ResponseEntity.noContent().build();
    }
}