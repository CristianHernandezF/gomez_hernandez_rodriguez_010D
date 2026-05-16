package com.nubemedica.service_login.exceptions;

public class JwtGenerationException extends RuntimeException {
    public JwtGenerationException(String message) { super(message);
    }
}
