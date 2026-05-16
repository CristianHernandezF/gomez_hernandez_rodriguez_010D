package com.nubemedica.service_login.exceptions;

import com.nubemedica.service_login.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<?> handleTokenRevoked(TokenRevokedException ex, HttpServletRequest request) {
        var error = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Sesión Invalida",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        var error = new ExceptionResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Usuario no encontrado",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> handleAuthException(AuthException ex, HttpServletRequest request) {
        var error = new ExceptionResponse(
                LocalDateTime.now(),
                409,
                "Error en authentication",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}