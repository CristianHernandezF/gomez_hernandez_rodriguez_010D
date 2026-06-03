package com.hospital.service_direccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DireccionResponse {

    private Long idDireccion;
    private String nombre;
    private String comuna;
    private String region;
    
}