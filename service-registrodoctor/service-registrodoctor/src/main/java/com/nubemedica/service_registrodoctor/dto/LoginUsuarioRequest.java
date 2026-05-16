package com.nubemedica.service_registrodoctor.dto;

public record LoginUsuarioRequest(
        String runDoctor,
        String correo,
        String contrasena,
        String numTelefono
) {
}
