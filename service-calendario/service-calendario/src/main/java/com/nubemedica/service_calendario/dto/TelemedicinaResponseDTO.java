package com.nubemedica.service_calendario.dto;

import lombok.Data;

@Data
public class TelemedicinaResponseDTO {
    private Long idSesionTelemedicina;
    private String linkAcceso;
    private String codigoAcceso;
}
