package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class FarmacosRecetadosDTO {
    @NotBlank(message = "El nombre del fármaco es obligatorio")
    private String nombreFarmaco;

    @Min(value = 0, message = "La dosis no puede ser negativa")
    private float dosis;

    public String getNombreFarmaco() {
        return nombreFarmaco;
    }

    public void setNombreFarmaco(String nombreFarmaco) {
        this.nombreFarmaco = nombreFarmaco;
    }

    public float getDosis() {
        return dosis;
    }

    public void setDosis(float dosis) {
        this.dosis = dosis;
    }

    
}