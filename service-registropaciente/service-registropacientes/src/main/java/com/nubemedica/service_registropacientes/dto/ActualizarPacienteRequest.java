package com.nubemedica.service_registropacientes.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarPacienteRequest {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String correo;

    @NotBlank(message = "El primer nombre es obligatorio")
    private String priNombre;

    private String segNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apaPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apaMaterno;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 15)
    private String numTelefono;


    private DireccionRequest direccion; // Renombrado de direccionDTO a direccion
}
