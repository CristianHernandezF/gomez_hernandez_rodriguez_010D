package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class TelefonoEmergenciaDTO {
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(min = 9, max = 15, message = "El teléfono debe tener entre 9 y 15 dígitos")
    private String numTelefono;

    @NotBlank(message = "La descripción (ej: Madre) es obligatoria")
    private String descripcion;

    public String getNumTelefono() {
        return numTelefono;
    }

    public void setNumTelefono(String numTelefono) {
        this.numTelefono = numTelefono;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    
}