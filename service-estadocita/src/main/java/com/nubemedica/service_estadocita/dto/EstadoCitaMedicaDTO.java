package com.nubemedica.service_estadocita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoCitaMedicaDTO {

    private String nombreEstado; // "Agendada", "Completada", "Cancelada"
    private String observaciones; 

}
