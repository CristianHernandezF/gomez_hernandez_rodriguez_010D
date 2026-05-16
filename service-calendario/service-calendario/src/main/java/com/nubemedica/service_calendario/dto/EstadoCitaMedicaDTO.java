package com.nubemedica.service_calendario.dto;
import lombok.Data;

@Data
public class EstadoCitaMedicaDTO {

    private String nombreEstado; // "Agendada", "Completada", "Cancelada"
    private String observaciones; // Opcional, para comentarios adicionales

    public EstadoCitaMedicaDTO() {
    }

    public EstadoCitaMedicaDTO(String nombreEstado, String observaciones) {
        this.nombreEstado = nombreEstado;
        this.observaciones = observaciones;
    }
}