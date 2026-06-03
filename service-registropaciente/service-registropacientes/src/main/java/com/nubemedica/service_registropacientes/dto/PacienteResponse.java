package com.nubemedica.service_registropacientes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PacienteResponse {
    private String runPaciente;
    private String nombreCompleto;
    private String correo;
    private String numTelefono;
    private DireccionResponse direccion; // Tu DTO normalizado
}
