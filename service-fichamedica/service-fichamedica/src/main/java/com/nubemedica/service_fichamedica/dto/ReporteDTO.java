package com.nubemedica.service_fichamedica.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReporteDTO {

    private Long idReporte;
    private String nombreReporte;
    private LocalDate fechaReporte;
    private Long idFichaMedica;
    private String descripcion;
}