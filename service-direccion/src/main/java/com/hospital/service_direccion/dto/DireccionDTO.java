package com.hospital.service_direccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DireccionDTO {

    private String nombre;
    private Long comunaId;
}
