package com.nubemedica.service_login.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String correo,
        @NotBlank @Size(min = 8) String contrasena,
        @NotBlank String numTelefono,
        @NotBlank String runDoctor
) {
}
