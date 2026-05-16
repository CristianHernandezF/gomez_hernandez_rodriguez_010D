package com.nubemedica.service_registropacientes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacienteResumenDTO {
    private String runPaciente;
    private String nombres;
    private String apellidos;
}
