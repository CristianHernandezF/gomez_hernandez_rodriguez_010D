package com.nubemedica.service_login.dto;

public record LoginUsuarioResponse(
        Long idUsuario,
        String correo,
        String numTelefono,
        String runDoctor
) {
}
