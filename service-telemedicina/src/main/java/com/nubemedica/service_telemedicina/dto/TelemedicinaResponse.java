package com.nubemedica.service_telemedicina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TelemedicinaResponse {

    private Long idSesionTelemedicina;
    private String linkAcceso;
    private String codigoAcceso;

}
