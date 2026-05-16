package com.nubemedica.service_calendario.controller;

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.service.CalendarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendario")
public class CalendarioController {

    @Autowired
    private CalendarioService calendarioService;

    // ==========================================
    // ENDPOINTS: CITAS MÉDICAS
    // ==========================================

@PostMapping("/citas")
    public ResponseEntity<CitaMedicaResponseDTO> crearCita(
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return new ResponseEntity<>(calendarioService.crearCitaMedica(request, runDoctor), HttpStatus.CREATED);
    }

    @GetMapping("/citas/{id}")
    public ResponseEntity<CitaMedicaResponseDTO> obtenerCita(@PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(calendarioService.obtenerCitaMedicaPorId(id, runDoctor));
    }

    @GetMapping("/citas/mias")
    public ResponseEntity<List<CitaMedicaResponseDTO>> listarCitasPorDoctor(@RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(calendarioService.listarCitasPorDoctor(runDoctor));
    }

    @PutMapping("/citas/{id}")
    public ResponseEntity<CitaMedicaResponseDTO> actualizarCita( @PathVariable Long id, 
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(calendarioService.actualizarCitaMedica(id, request, runDoctor));
    }

    @DeleteMapping("/citas/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        calendarioService.eliminarCitaMedica(id, runDoctor);
        return ResponseEntity.noContent().build();
    }


    // ENDPOINTS: ACTIVIDADES PERSONALES

    @PostMapping("/actividades")
    public ResponseEntity<ActividadPersonalResponseDTO> crearActividad( 
            @Valid @RequestBody ActividadPersonalRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return new ResponseEntity<>(calendarioService.crearActividadPersonal(request, runDoctor), HttpStatus.CREATED);
    }

    @GetMapping("/actividades/{id}")
    public ResponseEntity<ActividadPersonalResponseDTO> obtenerActividad(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(calendarioService.obtenerActividadPersonalPorId(id, runDoctor));
    }

    // Cambiamos la ruta para que sea "mias" y no pida el RUN por URL
    @GetMapping("/actividades/mias")
    public ResponseEntity<List<ActividadPersonalResponseDTO>> listarMisActividades(
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(calendarioService.listarActividadesPorDoctor(runDoctor));
    }

    @PutMapping("/actividades/{id}")
    public ResponseEntity<ActividadPersonalResponseDTO> actualizarActividad(
            @PathVariable Long id, 
            @Valid @RequestBody ActividadPersonalRequestDTO request,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        return ResponseEntity.ok(calendarioService.actualizarActividadPersonal(id, request, runDoctor));
    }

    @DeleteMapping("/actividades/{id}")
    public ResponseEntity<Void> eliminarActividad(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctor) {
        calendarioService.eliminarActividadPersonal(id, runDoctor);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ENDPOINT: AGENDA GENERAL DEL DOCTOR
    // ==========================================

    @GetMapping("/agenda/doctor")
    public ResponseEntity<List<EventoDTO>> obtenerAgendaCompleta(@RequestHeader("X-Doctor-Run") String runDoctor) {
        // Este endpoint devuelve Citas y Actividades mezcladas en orden cronológico
        List<EventoDTO> agenda = calendarioService.obtenerAgendaDoctor(runDoctor);
        return ResponseEntity.ok(agenda);
    }

    @DeleteMapping("/citas/doctor/{runDoctor}/paciente/{runPaciente}")
    public ResponseEntity<Void> eliminarCitasRelacion(
            @PathVariable String runDoctor, 
            @PathVariable String runPaciente) {
        calendarioService.eliminarCitasEntreDoctorYPaciente(runDoctor, runPaciente);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/agenda/doctor/{runDoctor}")
    public ResponseEntity<Void> limpiarAgendaDoctor(@PathVariable String runDoctor) {
        calendarioService.eliminarTodaLaAgendaDelDoctor(runDoctor);
        return ResponseEntity.noContent().build();
    }
}
