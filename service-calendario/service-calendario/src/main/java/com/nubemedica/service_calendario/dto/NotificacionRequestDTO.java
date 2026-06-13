package com.nubemedica.service_calendario.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class NotificacionRequestDTO {
    private String correoDestino;
    private String asunto;
    private String mensaje;
    private LocalDate fechaEnvio;
    private LocalTime horaEnvio;
    
    public NotificacionRequestDTO(String correoDestino, String asunto, String mensaje, LocalDate fechaEnvio,
            LocalTime horaEnvio) {
        this.correoDestino = correoDestino;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.horaEnvio = horaEnvio;
    }

    
}
