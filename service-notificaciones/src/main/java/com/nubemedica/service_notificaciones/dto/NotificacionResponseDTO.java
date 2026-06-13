package com.nubemedica.service_notificaciones.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class NotificacionResponseDTO {
    private Long idNotificacion;
    private String correoDestino;
    private String asunto;
    private String mensaje;
    private LocalDate fechaEnvio;
    private LocalTime horaEnvio;
    private boolean estadoEnvio;
}