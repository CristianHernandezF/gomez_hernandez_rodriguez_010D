package com.nubemedica.service_registrodoctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrarDoctorRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String correo;

    @NotBlank(message = "El RUN es obligatorio")
    private String runDoctor;

    @NotBlank(message = "El primer nombre es obligatorio")
    private String priNombre;

    private String segNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apaPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apaMaterno;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;


    private DireccionDTO direccion;
}
