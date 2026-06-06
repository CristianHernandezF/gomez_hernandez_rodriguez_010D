package com.nubemedica.service_fichamedica.dto;

import lombok.Data;

@Data
public class PacienteDTO {
    private String runPaciente;
    private String nombreCompleto;
    private String correo;
    private String numTelefono;
}