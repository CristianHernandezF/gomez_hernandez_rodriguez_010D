package com.nubemedica.service_fichamedica.controller;

import com.nubemedica.service_fichamedica.dto.FichaMedicaResponse;
import com.nubemedica.service_fichamedica.dto.FichaMedicaUpdateRequest;
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

    // =====================================================================
    // POST /api/v1/fichas?runPaciente=XX&runDoctor=YY
    // Crea una ficha médica vacía para el par paciente-doctor
    // =====================================================================
    @PostMapping
    public ResponseEntity<FichaMedicaResponse> crearFicha(
            @RequestParam String runPaciente,
            @RequestParam String runDoctor) {

        FichaMedicaResponse response = fichaMedicaService.crearFicha(runPaciente, runDoctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================================
    // GET /api/v1/fichas/{id}
    // Obtiene una ficha médica por su ID
    // =====================================================================
    @GetMapping("/{id}")
    public ResponseEntity<FichaMedicaResponse> obtenerFichaPorId(@PathVariable Long id) {

        FichaMedicaResponse response = fichaMedicaService.obtenerFichaPorId(id);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // GET /api/v1/fichas/buscar?runPaciente=XX&runDoctor=YY
    // Busca la ficha específica de un paciente con un doctor
    // =====================================================================
    @GetMapping("/buscar")
    public ResponseEntity<FichaMedicaResponse> obtenerFichaPorPacienteYDoctor(
            @RequestParam String runPaciente,
            @RequestParam String runDoctor) {

        FichaMedicaResponse response = fichaMedicaService
                .obtenerFichaPorPacienteYDoctor(runPaciente, runDoctor);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // GET /api/v1/fichas/doctor/{runDoctor}
    // Lista todas las fichas que tiene un doctor
    // =====================================================================
    @GetMapping("/doctor/{runDoctor}")
    public ResponseEntity<List<FichaMedicaResponse>> listarFichasPorDoctor(
            @PathVariable String runDoctor) {

        List<FichaMedicaResponse> response = fichaMedicaService.listarFichasPorDoctor(runDoctor);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // PUT /api/v1/fichas/{id}
    // Actualiza diagnóstico, historial, fármacos, contactos y teléfonos
    // =====================================================================
    @PutMapping("/{id}")
    public ResponseEntity<FichaMedicaResponse> actualizarFicha(
            @PathVariable Long id,
            @Valid @RequestBody FichaMedicaUpdateRequest request) {

        FichaMedicaResponse response = fichaMedicaService.actualizarFicha(id, request);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // DELETE /api/v1/fichas/{id}
    // Elimina una ficha médica por su ID
    // =====================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFicha(@PathVariable Long id) {

        fichaMedicaService.eliminarFicha(id);
        return ResponseEntity.noContent().build();
    }
}