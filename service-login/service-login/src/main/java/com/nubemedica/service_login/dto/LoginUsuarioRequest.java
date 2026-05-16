package com.nubemedica.service_login.dto;

public record LoginUsuarioRequest(
        String correo,
        String contrasena
) {
}
