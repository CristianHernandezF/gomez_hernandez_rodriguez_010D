package com.nubemedica.service_reportes.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nubemedica.service_reportes.dto.ReporteRequest;
import com.nubemedica.service_reportes.dto.ReporteResponse;
import com.nubemedica.service_reportes.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_reportes.model.DetalleReporte;
import com.nubemedica.service_reportes.model.Reporte;
import com.nubemedica.service_reportes.repository.ReporteRepository;

@ExtendWith(MockitoExtension.class)
public class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    @Test
    void crearReporte_Success() {
        ReporteRequest request = new ReporteRequest();
        request.setNombreReporte("Examen de Sangre");
        request.setDescripcion("Niveles de glucosa normales");
        request.setIdFichaMedica(1L);

        when(reporteRepository.save(any(Reporte.class))).thenAnswer(i -> {
            Reporte r = i.getArgument(0);
            r.setIdReporte(10L); // Simula ID generado
            return r;
        });

        ReporteResponse response = reporteService.crearReporte(request);

        assertNotNull(response);
        assertEquals(10L, response.getIdReporte());
        assertEquals("Examen de Sangre", response.getNombreReporte());
        assertEquals("Niveles de glucosa normales", response.getDescripcion());
        assertEquals(LocalDate.now(), response.getFechaReporte()); // Verifica que el sistema asignó la fecha
        verify(reporteRepository).save(any());
    }

    @Test
    void obtenerReportePorId_Success() {
        Reporte reporte = new Reporte();
        reporte.setIdReporte(10L);
        reporte.setNombreReporte("Radiografía");
        
        DetalleReporte detalle = new DetalleReporte();
        detalle.setDescripcion("Sin hallazgos patológicos");
        reporte.setDetalleReporte(detalle);

        when(reporteRepository.findById(10L)).thenReturn(Optional.of(reporte));

        ReporteResponse response = reporteService.obtenerReportePorId(10L);

        assertNotNull(response);
        assertEquals("Radiografía", response.getNombreReporte());
        assertEquals("Sin hallazgos patológicos", response.getDescripcion());
    }

    @Test
    void obtenerReportePorId_NotFound_ThrowsException() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            reporteService.obtenerReportePorId(1L);
        });
    }

    @Test
    void listarReportesPorFicha_Success() {
        Long idFicha = 1L;
        Reporte r1 = new Reporte();
        r1.setIdFichaMedica(idFicha);
        r1.setDetalleReporte(new DetalleReporte());
        
        when(reporteRepository.findByIdFichaMedica(idFicha)).thenReturn(List.of(r1));

        List<ReporteResponse> lista = reporteService.listarReportesPorFicha(idFicha);

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        verify(reporteRepository).findByIdFichaMedica(idFicha);
    }

    @Test
    void actualizarReporte_Success() {
        Long id = 10L;
        Reporte reporteExistente = new Reporte();
        reporteExistente.setIdReporte(id);
        reporteExistente.setNombreReporte("Antiguo");
        
        DetalleReporte detalle = new DetalleReporte();
        detalle.setDescripcion("Antigua");
        reporteExistente.setDetalleReporte(detalle);

        ReporteRequest request = new ReporteRequest();
        request.setNombreReporte("Nuevo Nombre");
        request.setDescripcion("Nueva Descripcion");

        when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteExistente));
        when(reporteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReporteResponse response = reporteService.actualizarReporte(id, request);

        assertEquals("Nuevo Nombre", response.getNombreReporte());
        assertEquals("Nueva Descripcion", response.getDescripcion());
        verify(reporteRepository).save(any());
    }

    @Test
    void eliminarReporte_Success() {
        when(reporteRepository.existsById(1L)).thenReturn(true);

        reporteService.eliminarReporte(1L);

        verify(reporteRepository).deleteById(1L);
    }
}