package com.nubemedica.service_fichamedica.controller;

import com.nubemedica.service_fichamedica.dto.FichaMedicaResponse;
import com.nubemedica.service_fichamedica.dto.FichaMedicaUpdateRequest;
import com.nubemedica.service_fichamedica.dto.ReporteCreateRequest;
import com.nubemedica.service_fichamedica.dto.ReporteDTO;
import com.nubemedica.service_fichamedica.dto.ReporteUpdateRequest;
import com.nubemedica.service_fichamedica.service.FichaMedicaService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fichas")
@CrossOrigin(origins = "http://localhost:8081")
public class FichaMedicaController {

    @Autowired
    private FichaMedicaService fichaMedicaService;

    // POST /api/v1/fichas?runPaciente=XX
    @PostMapping
    @Operation(summary = "Crear una  ficha medica para un paciente",description = "Requiere autentificacion del doctor, rut de doctor y rut de paciente")
    public ResponseEntity<FichaMedicaResponse> crearFicha(
            @RequestParam String runPaciente,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fichaMedicaService.crearFicha(runPaciente, runDoctorToken));
    }

    @PostMapping("/interno")
    @Operation(summary = "Crear una ficha internamente de un paciente")
    public ResponseEntity<Void> crearFichaInterno(
            @RequestParam String runPaciente,
            @RequestParam String runDoctor) {

        fichaMedicaService.crearFichaInterno(runPaciente, runDoctor);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/v1/fichas/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una ficha medica por su ID",description = "Se requiere ID ficha, RUT doctor, Autentificacion de Doctor")
    public ResponseEntity<FichaMedicaResponse> obtenerFichaPorId(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctorToken,
            @RequestHeader("Authorization") String token
        ) {

        return ResponseEntity.ok(fichaMedicaService.obtenerFichaPorId(id, runDoctorToken, token));
    }

    // GET /api/v1/fichas/buscar?runPaciente=XX
    @GetMapping("/buscar")
    @Operation(summary = "Obtener ficha medica por rut de paciente y rut de doctor",description = "Se requiere RUT paciente, Rut Doctor, token del doctor")
    public ResponseEntity<FichaMedicaResponse> obtenerFichaPorPacienteYDoctor(
            @RequestParam String runPaciente,
            @RequestHeader("X-Doctor-Run") String runDoctorToken,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(fichaMedicaService
                .obtenerFichaPorPacienteYDoctor(runPaciente, runDoctorToken, token)); 
    }

    // GET /api/v1/fichas/doctor
    @GetMapping("/doctor")
    @Operation(summary = "Listar todas las fichas de un doctor",description = "Dado un rut del doctor y su token se listan todas las fichas medicas")
    public ResponseEntity<List<FichaMedicaResponse>> listarFichasPorDoctor(
            @RequestHeader("X-Doctor-Run") String runDoctorToken,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(fichaMedicaService.listarFichasPorDoctor(runDoctorToken, token));
    }

    // PUT /api/v1/fichas/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ficha medica dado su ID",description = "Se requiere ID ficha,token, autentificacion, Rut de doctor")
    public ResponseEntity<FichaMedicaResponse> actualizarFicha(
            @PathVariable Long id,
            @Valid @RequestBody FichaMedicaUpdateRequest request,
            @RequestHeader("X-Doctor-Run") String runDoctorToken,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(fichaMedicaService.actualizarFicha(id, request, runDoctorToken, token));
    }

    // DELETE /api/v1/fichas/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ficha medica de un doctor por ID")
    public ResponseEntity<Void> eliminarFicha(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        fichaMedicaService.eliminarFicha(id, runDoctorToken);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/fichas/{idFicha}/reportes
    @PostMapping("/{idFicha}/reportes")
    @Operation(summary = "Agregar un reporte a una ficha medica", description = "Se requiere ID ficha, body del reporte, rut del doctor")
    public ResponseEntity<ReporteDTO> agregarReporte(
            @PathVariable Long idFicha,
            @Valid @RequestBody ReporteCreateRequest request,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fichaMedicaService.agregarReporte(idFicha, request, runDoctorToken));
    }

    // GET /api/v1/fichas/{idFicha}/reportes/{idReporte}
    @GetMapping("/{idFicha}/reportes/{idReporte}")
    @Operation(summary = "Obtener un reporte de un ficha especifica",description = "Se requiere ID ficha,ID reporte y RUT de doctor")
    public ResponseEntity<ReporteDTO> obtenerReporte(
            @PathVariable Long idFicha,
            @PathVariable Long idReporte,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.ok(fichaMedicaService
                .obtenerReporteDeFicha(idFicha, idReporte, runDoctorToken));
    }

    // PUT /api/v1/fichas/{idFicha}/reportes/{idReporte}
    @PutMapping("/{idFicha}/reportes/{idReporte}")
    @Operation(summary = "Editar un reporte",description = "Requiere ID ficha, ID reporte, BODY de reporte y RUT de doctor")
    public ResponseEntity<ReporteDTO> editarReporte(
            @PathVariable Long idFicha,
            @PathVariable Long idReporte,
            @Valid @RequestBody ReporteUpdateRequest request,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.ok(fichaMedicaService
                .editarReporteDeFicha(idFicha, idReporte, request, runDoctorToken));
    }

    // DELETE /api/v1/fichas/{idFicha}/reportes/{idReporte}
    @DeleteMapping("/{idFicha}/reportes/{idReporte}")
    @Operation(summary = "Eliminar un reporte por su ID",description = "Requiere ID ficha, ID reporte, RUT de doctor")
    public ResponseEntity<Void> eliminarReporte(
            @PathVariable Long idFicha,
            @PathVariable Long idReporte,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        fichaMedicaService.eliminarReporteDeFicha(idFicha, idReporte, runDoctorToken);
        return ResponseEntity.noContent().build();
    }
}