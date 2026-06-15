package com.nubemedica.service_notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class NotificacionRequestDTO {

    @Email(message = "El formato del correo es inválido")
    @NotBlank(message = "El correo de destino es obligatorio")
    private String correoDestino;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    @NotNull(message = "La fecha de envío es obligatoria")
    private LocalDate fechaEnvio;

    @NotNull(message = "La hora de envío es obligatoria")
    private LocalTime horaEnvio;

    @NotNull(message = "El ID del evento es obligatorio")
    private Long idEvento; 
}  