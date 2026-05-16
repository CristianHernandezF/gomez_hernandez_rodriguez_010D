package com.nubemedica.service_login.dto;

import java.time.LocalDateTime;

public record ConsultaTokenResponse(
        Long idConsulta,
        Long usuarioId,
        String correo,
        String nomApi,
        LocalDateTime fechaConsulta
) {
}
