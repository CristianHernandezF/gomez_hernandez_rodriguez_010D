package com.hospital.service_direccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DireccionRequest {

    private String nombre;
    private String comuna;
    private String region;
}
