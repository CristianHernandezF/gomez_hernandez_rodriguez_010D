package com.nubemedica.service_registrodoctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarDoctorRequest {

    @NotBlank(message = "El primer nombre es obligatorio")
    private String priNombre;

    private String segNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apaPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apaMaterno;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;


    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String correo;

    // DIRECCIÓN
    @NotNull(message = "La dirección es obligatoria")
    private DireccionDTO direccion;
}
