package com.nubemedica.service_registrodoctor.dto;// Ajustar paquete según corresponda

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponse {
    private Long idDireccion;
    private String nombre;
    private String region;
    private String comuna;
}
