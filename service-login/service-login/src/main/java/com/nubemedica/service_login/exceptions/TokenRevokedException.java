package com.nubemedica.service_login.exceptions;

public class TokenRevokedException extends RuntimeException {
    public TokenRevokedException(String message) {
        super(message);
    }
}
