package com.nubemedica.service_registropacientes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor 
public class DireccionDTO {
    @NotBlank(message = "El nombre de la calle es obligatorio")
    private String nombre;

    @NotNull(message = "La comuna es obligatoria")
    private Long comunaId;
}
