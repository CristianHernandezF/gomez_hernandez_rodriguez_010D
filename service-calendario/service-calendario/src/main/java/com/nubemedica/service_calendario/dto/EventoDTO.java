package com.nubemedica.service_calendario.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class EventoDTO {

    private Long idEvento;
    private LocalDate fecha;
    private LocalTime hora;
    private String tipo; // "Cita Medica" o "Actividad Personal"
    private String color;
    private String runDoctor;

    //Para citas medicas
    private String runPaciente;
    private String motivoConsulta;
    private EstadoCitaMedicaDTO estadoCitaMedica; //solo para citas

    //Para actividadesPersonales
    private String nombreActividad;
    private String descripcion;

}
