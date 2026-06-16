package com.nubemedica.service_fichamedica.controller;

import com.nubemedica.service_fichamedica.dto.FichaMedicaResponse;
import com.nubemedica.service_fichamedica.dto.FichaMedicaUpdateRequest;
import com.nubemedica.service_fichamedica.dto.ReporteCreateRequest;
import com.nubemedica.service_fichamedica.dto.ReporteDTO;
import com.nubemedica.service_fichamedica.dto.ReporteUpdateRequest;
import com.nubemedica.service_fichamedica.service.FichaMedicaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fichas")
public class FichaMedicaController {

    @Autowired
    private FichaMedicaService fichaMedicaService;

    // POST /api/v1/fichas?runPaciente=XX
    @PostMapping
    public ResponseEntity<FichaMedicaResponse> crearFicha(
            @RequestParam String runPaciente,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fichaMedicaService.crearFicha(runPaciente, runDoctorToken));
    }

    @PostMapping("/interno")
    public ResponseEntity<Void> crearFichaInterno(
            @RequestParam String runPaciente,
            @RequestParam String runDoctor) {

        fichaMedicaService.crearFichaInterno(runPaciente, runDoctor);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/v1/fichas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FichaMedicaResponse> obtenerFichaPorId(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctorToken,
            @RequestHeader("Authorization") String token
        ) {

        return ResponseEntity.ok(fichaMedicaService.obtenerFichaPorId(id, runDoctorToken, token));
    }

    // GET /api/v1/fichas/buscar?runPaciente=XX
    @GetMapping("/buscar")
    public ResponseEntity<FichaMedicaResponse> obtenerFichaPorPacienteYDoctor(
            @RequestParam String runPaciente,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.ok(fichaMedicaService
                .obtenerFichaPorPacienteYDoctor(runPaciente, runDoctorToken)); 
    }

    // GET /api/v1/fichas/doctor
    @GetMapping("/doctor")
    public ResponseEntity<List<FichaMedicaResponse>> listarFichasPorDoctor(
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.ok(fichaMedicaService.listarFichasPorDoctor(runDoctorToken));
    }

    // PUT /api/v1/fichas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<FichaMedicaResponse> actualizarFicha(
            @PathVariable Long id,
            @Valid @RequestBody FichaMedicaUpdateRequest request,
            @RequestHeader("X-Doctor-Run") String runDoctorToken,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(fichaMedicaService.actualizarFicha(id, request, runDoctorToken, token));
    }

    // DELETE /api/v1/fichas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFicha(
            @PathVariable Long id,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        fichaMedicaService.eliminarFicha(id, runDoctorToken);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/fichas/{idFicha}/reportes
    @PostMapping("/{idFicha}/reportes")
    public ResponseEntity<ReporteDTO> agregarReporte(
            @PathVariable Long idFicha,
            @Valid @RequestBody ReporteCreateRequest request,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fichaMedicaService.agregarReporte(idFicha, request, runDoctorToken));
    }

    // GET /api/v1/fichas/{idFicha}/reportes/{idReporte}
    @GetMapping("/{idFicha}/reportes/{idReporte}")
    public ResponseEntity<ReporteDTO> obtenerReporte(
            @PathVariable Long idFicha,
            @PathVariable Long idReporte,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        return ResponseEntity.ok(fichaMedicaService
                .obtenerReporteDeFicha(idFicha, idReporte, runDoctorToken));
    }

    // PUT /api/v1/fichas/{idFicha}/reportes/{idReporte}
    @PutMapping("/{idFicha}/reportes/{idReporte}")
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
    public ResponseEntity<Void> eliminarReporte(
            @PathVariable Long idFicha,
            @PathVariable Long idReporte,
            @RequestHeader("X-Doctor-Run") String runDoctorToken) {

        fichaMedicaService.eliminarReporteDeFicha(idFicha, idReporte, runDoctorToken);
        return ResponseEntity.noContent().build();
    }
}