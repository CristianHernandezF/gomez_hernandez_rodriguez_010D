package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactoProfesional{
    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String nombres;
    
    @NotBlank(message = "El apellido del contacto es obligatorio")
    private String apellidos;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo de contacto es obligatorio")
    private String correo;
}