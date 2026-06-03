package com.nubemedica.service_registrodoctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DireccionRequest {
    @NotBlank(message = "El nombre de la calle/nro es obligatorio")
    private String nombre;
    @NotBlank(message = "La comuna es obligatoria")
    private String comuna; // El usuario envía el texto "Providencia"
    @NotBlank(message = "La región es obligatoria")
    private String region; // El usuario envía el texto "Metropolitana"
}
