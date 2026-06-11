package com.nubemedica.service_reportes.service;

import com.nubemedica.service_reportes.dto.ReporteRequest;
import com.nubemedica.service_reportes.dto.ReporteResponse;
import com.nubemedica.service_reportes.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_reportes.model.DetalleReporte;
import com.nubemedica.service_reportes.model.Reporte;
import com.nubemedica.service_reportes.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    // =====================================================================
    // CREAR REPORTE
    // =====================================================================
    @Transactional
    public ReporteResponse crearReporte(ReporteRequest request) {

        // 1. Crear el detalle primero (será persistido por cascade desde Reporte)
        DetalleReporte detalle = new DetalleReporte();
        detalle.setDescripcion(request.getDescripcion());

        // 2. Crear el reporte y asociarle el detalle
        Reporte reporte = new Reporte();
        reporte.setNombreReporte(request.getNombreReporte());
        reporte.setFechaReporte(LocalDate.now()); // La fecha la asigna el sistema
        reporte.setIdFichaMedica(request.getIdFichaMedica());
        reporte.setDetalleReporte(detalle);

        // 3. Un solo save() persiste Reporte + DetalleReporte (por CascadeType.ALL)
        return mapearAResponse(reporteRepository.save(reporte));
    }

    // =====================================================================
    // OBTENER REPORTE POR ID
    // =====================================================================
    @Transactional(readOnly = true)
    public ReporteResponse obtenerReportePorId(Long id) {
        Reporte reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el reporte con ID: " + id));

        return mapearAResponse(reporte);
    }

    // =====================================================================
    // LISTAR REPORTES POR FICHA MÉDICA
    // Este método es llamado por service-fichamedica via WebClient
    // =====================================================================
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarReportesPorFicha(Long idFichaMedica) {
        return reporteRepository.findByIdFichaMedica(idFichaMedica)
            .stream()
            .map(this::mapearAResponse)
            .collect(Collectors.toList());
    }

    // =====================================================================
    // ACTUALIZAR DESCRIPCIÓN DEL REPORTE
    // =====================================================================
    @Transactional
    public ReporteResponse actualizarReporte(Long id, ReporteRequest request) {
        Reporte reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el reporte con ID: " + id));

        reporte.setNombreReporte(request.getNombreReporte());
        reporte.getDetalleReporte().setDescripcion(request.getDescripcion());
        // La fecha NO se actualiza — conserva la fecha original de la cita

        return mapearAResponse(reporteRepository.save(reporte));
    }

    // =====================================================================
    // ELIMINAR REPORTE
    // =====================================================================
    @Transactional
    public void eliminarReporte(Long id) {
        if (!reporteRepository.existsById(id))
            throw new RecursoNoEncontradoException(
                "No se encontró el reporte con ID: " + id);

        reporteRepository.deleteById(id);
        // DetalleReporte se elimina automáticamente por orphanRemoval = true
    }

    // =====================================================================
    // MÉTODO PRIVADO: mapear Model → DTO de salida
    // =====================================================================
    private ReporteResponse mapearAResponse(Reporte reporte) {
        return ReporteResponse.builder()
            .idReporte(reporte.getIdReporte())
            .nombreReporte(reporte.getNombreReporte())
            .fechaReporte(reporte.getFechaReporte())
            .idFichaMedica(reporte.getIdFichaMedica())
            .descripcion(reporte.getDetalleReporte() != null
                ? reporte.getDetalleReporte().getDescripcion()
                : null)
            .build();
    }
}