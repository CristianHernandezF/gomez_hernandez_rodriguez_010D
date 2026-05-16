package com.nubemedica.service_calendario.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class CitaMedicaRequestDTO {
    private String runPaciente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivoConsulta;
    
    // Agregamos esto para permitir actualizar el estado
    private EstadoCitaMedicaDTO estadoCitaMedica; 
}
