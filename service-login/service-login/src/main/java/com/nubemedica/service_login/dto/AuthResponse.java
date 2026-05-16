package com.nubemedica.service_login.dto;

public record AuthResponse(
        String runDoctor,
        String correo,
        String token,
        String refreshToken
) {
}
