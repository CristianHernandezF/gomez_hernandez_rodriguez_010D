package com.nubemedica.service_calendario.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class CitaMedicaResponseDTO {
    private Long idEvento;

    private String runPaciente;
    private String runDoctor;

    private LocalDate fecha;
    private LocalTime hora;
    private String motivoConsulta;
    private EstadoCitaMedicaDTO estadoCita;
}
