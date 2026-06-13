package com.nubemedica.service_calendario.dto;

import lombok.Data;

@Data
public class DoctorResponseDTO {
    private String runDoctor;
    private String correo;
    private String nombreCompleto; // El MS-Doctor ya lo entrega armado
}
