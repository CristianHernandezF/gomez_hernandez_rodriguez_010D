package com.nubemedica.service_calendario.dto;

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