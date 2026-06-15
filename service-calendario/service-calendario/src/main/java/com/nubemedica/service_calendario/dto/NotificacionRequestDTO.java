package com.nubemedica.service_calendario.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionRequestDTO {
    private String correoDestino;
    private String asunto;
    private String mensaje;
    private LocalDate fechaEnvio;
    private LocalTime horaEnvio;
    private Long idEvento;
}
