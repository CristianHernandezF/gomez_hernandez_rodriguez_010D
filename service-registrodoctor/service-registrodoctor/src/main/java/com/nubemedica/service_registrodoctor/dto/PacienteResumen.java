package com.nubemedica.service_registrodoctor.dto;


import lombok.Data;

@Data
public class PacienteResumen {
    private String runPaciente;
    private String nombres;   // Aquí irán los dos nombres
    private String apellidos; // Aquí irán los dos apellidos
}
