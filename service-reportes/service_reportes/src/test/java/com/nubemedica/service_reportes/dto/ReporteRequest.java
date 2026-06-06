package com.nubemedica.service_reportes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReporteRequest {

    @NotBlank(message = "El nombre del reporte es obligatorio")
    private String nombreReporte;

    @NotNull(message = "Debe indicar a qué ficha médica pertenece")
    private Long idFichaMedica;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
}
