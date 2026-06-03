package com.nubemedica.service_registrodoctor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponse {
    private String runDoctor;
    private String correo;

   private String nombreCompleto;

    private String telefono;
    private DireccionResponse direccion; // La dirección plana

}
