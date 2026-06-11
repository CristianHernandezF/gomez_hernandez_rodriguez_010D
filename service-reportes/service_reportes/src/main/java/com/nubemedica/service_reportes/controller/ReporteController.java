package com.nubemedica.service_reportes.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nubemedica.service_reportes.dto.ReporteRequest;
import com.nubemedica.service_reportes.dto.ReporteResponse;
import com.nubemedica.service_reportes.service.ReporteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // =====================================================================
    // POST /api/v1/reportes
    // Llamado INTERNAMENTE por service-fichamedica via WebClient
    // El doctor NO llama a este endpoint directamente
    // =====================================================================
    @PostMapping
    public ResponseEntity<ReporteResponse> crearReporte(
            @Valid @RequestBody ReporteRequest request) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(reporteService.crearReporte(request));
    }

    // =====================================================================
    // GET /api/v1/reportes/{id}
    // Obtiene un reporte puntual por su ID
    // =====================================================================
    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(reporteService.obtenerReportePorId(id));
    }

    // =====================================================================
    // GET /api/v1/reportes/ficha/{idFichaMedica}
    // Llamado INTERNAMENTE por service-fichamedica via WebClient
    // Devuelve todos los reportes que pertenecen a una ficha
    // =====================================================================
    @GetMapping("/ficha/{idFichaMedica}")
    public ResponseEntity<List<ReporteResponse>> listarPorFicha(
            @PathVariable Long idFichaMedica) {

        return ResponseEntity.ok(reporteService.listarReportesPorFicha(idFichaMedica));
    }

    // =====================================================================
    // PUT /api/v1/reportes/{id}
    // Actualiza el nombre y descripción de un reporte existente
    // =====================================================================
    @PutMapping("/{id}")
    public ResponseEntity<ReporteResponse> actualizarReporte(
            @PathVariable Long id,
            @Valid @RequestBody ReporteRequest request) {

        return ResponseEntity.ok(reporteService.actualizarReporte(id, request));
    }

    // =====================================================================
    // DELETE /api/v1/reportes/{id}
    // Elimina un reporte y su DetalleReporte asociado
    // =====================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(
            @PathVariable Long id) {

        reporteService.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }
}