package com.nubemedica.service_calendario.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class ActividadPersonalResponseDTO {
    private Long idEvento;
    private LocalDate fecha;
    private LocalTime hora;
    private String nombreActividad;
    private String descripcion;

}
