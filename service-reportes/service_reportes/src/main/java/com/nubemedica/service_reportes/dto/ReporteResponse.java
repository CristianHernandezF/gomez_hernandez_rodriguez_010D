package com.nubemedica.service_reportes.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ReporteResponse {

    private Long idReporte;
    private String nombreReporte;
    private LocalDate fechaReporte;
    private Long idFichaMedica;
    private String descripcion;  // Descripción de DetalleReporte "aplanada"
}