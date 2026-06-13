package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReporteUpdateRequest {

    @NotBlank(message = "El nombre del reporte es obligatorio")
    private String nombreReporte;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
}
