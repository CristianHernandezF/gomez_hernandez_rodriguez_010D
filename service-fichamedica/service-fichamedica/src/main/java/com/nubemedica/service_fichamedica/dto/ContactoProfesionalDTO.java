package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ContactoProfesionalDTO{
    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String nombres;
    
    @NotBlank(message = "El apellido del contacto es obligatorio")
    private String apellidos;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo de contacto es obligatorio")
    private String correo;

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    
}