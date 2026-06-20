package com.nubemedica.service_calendario.controller;

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.service.ActividadPersonalService;
import com.nubemedica.service_calendario.service.CitaMedicaService;
import com.nubemedica.service_calendario.service.EventoService;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Crear una cita medica",description = "se requiere un body de la cita medica, Rut del doctor, autentificacion del doctor y token")
    public ResponseEntity<CitaMedicaResponseDTO> crearCita(
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(citaMedicaService.crearCitaMedica(request, runDoctor, token), HttpStatus.CREATED);
    }

    @GetMapping("/citas/{id}")
    @Operation(summary = "Obtener cita medica por ID",description = "Se requiere ID de cita, RUT del doctor, autentificacion")
    public ResponseEntity<CitaMedicaResponseDTO> obtenerCita(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(citaMedicaService.obtenerCitaMedicaPorId(id, runDoctor));
    }

    @GetMapping("/citas/mias")
    @Operation(summary = "Obtener todas las citas medicas del doctor.",description = "Se requiere token del doctor y RUT del doctor")
    public ResponseEntity<List<CitaMedicaResponseDTO>> listarCitasPorDoctor(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(citaMedicaService.listarCitasMedicasPorDoctor(runDoctor));
    }

    @PutMapping("/citas/{id}")
    @Operation(summary = "Actualizar cita medica por su ID",description = "Se requiere ID de la cita, body de la cita, RUT del doctor y token del doctor")
    public ResponseEntity<CitaMedicaResponseDTO> actualizarCita(
            @PathVariable Long id, 
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(citaMedicaService.actualizarCitaMedica(id, request, runDoctor, token));
    }

    @DeleteMapping("/citas/{id}")
    @Operation(summary = "Eliminar una cita por ID",description = "se requiere ID de la cita, RUT del doctor, y token del doctor")
    public ResponseEntity<Void> eliminarCita(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        citaMedicaService.eliminarCitaMedica(id, runDoctor, token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/citas/doctor/{runDoctor}/paciente/{runPaciente}")
    @Operation(summary = "Eliminar relacion de cita entre paciente y doctor",description = "se requiere RUT doctor y RUT paciente")
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
    @Operation(summary = "Crear actividades personales de un doctor",description = "se requiere body de actividades personal, RUT doctor, Token")
    public ResponseEntity<ActividadPersonalResponseDTO> crearActividad( 
            @Valid @RequestBody ActividadPersonalRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(actividadService.crearActividadPersonal(request, runDoctor, token), HttpStatus.CREATED);
    }

    @GetMapping("/actividades/{id}")
    @Operation(summary = "Obtener actividades personales por ID",description = "se requiere ID de actividad,RUT doctor, TOKEN doctor")
    public ResponseEntity<ActividadPersonalResponseDTO> obtenerActividad(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(actividadService.obtenerActividadPersonalPorId(id, runDoctor));
    }

    @GetMapping("/actividades/mias")
    @Operation(summary = "Obtener todas las actividades personales de un doctor",description = "Se requiere RUT del doctor y su token")
    public ResponseEntity<List<ActividadPersonalResponseDTO>> listarMisActividades(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(actividadService.listarActividadesPorDoctor(runDoctor));
    }

    @PutMapping("/actividades/{id}")
    @Operation(summary = "Actualizar actividad personal",description = "se requiere ID de actividad personales, BODY actividad personal, RUT doctor y autentificacion")
    public ResponseEntity<ActividadPersonalResponseDTO> actualizarActividad(
            @PathVariable Long id, 
            @Valid @RequestBody ActividadPersonalRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(actividadService.actualizarActividadPersonal(id, request, runDoctor, token));
    }

    @DeleteMapping("/actividades/{id}")
    @Operation(summary = "Eliminar actividad personal", description = "se requiere ID actividad personal, RUT doctor y token")
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
    @Operation(summary = "Obtener agenda completa de un doctor")
    public ResponseEntity<List<EventoDTO>> obtenerAgendaCompleta(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        // Este endpoint devuelve Citas y Actividades mezcladas en orden cronológico
        return ResponseEntity.ok(eventoService.obtenerAgendaDoctor(runDoctor));
    }

    @DeleteMapping("/agenda/doctor/{runDoctor}")
    @Operation(summary = "Limpiar la agenta de un doctor")
    public ResponseEntity<Void> limpiarAgendaDoctor(@PathVariable String runDoctor) {
        eventoService.eliminarTodaLaAgendaDelDoctor(runDoctor);
        return ResponseEntity.noContent().build();
    }
}