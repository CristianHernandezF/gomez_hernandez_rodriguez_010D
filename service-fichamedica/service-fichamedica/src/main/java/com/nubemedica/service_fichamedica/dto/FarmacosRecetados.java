package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FarmacosRecetados {
    @NotBlank(message = "El nombre del fármaco es obligatorio")
    private String nombreFarmaco;

    @Min(value = 0, message = "La dosis no puede ser negativa")
    private float dosis;
}