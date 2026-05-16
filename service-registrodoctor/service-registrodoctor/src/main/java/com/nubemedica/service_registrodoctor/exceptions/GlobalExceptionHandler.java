package com.nubemedica.service_registrodoctor.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleDatosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }

    @ExceptionHandler(NoExisteDoctorException.class)
    public ResponseEntity<Map<String, String>> handleNoExisteDoctor(NoExisteDoctorException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error); // 404
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleDatoDuplicado(DatoDuplicadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // 409
    }

    @ExceptionHandler(ComunicacionMicroservicioException.class)
    public ResponseEntity<Map<String, String>> handleComunicacionMicroservicio(ComunicacionMicroservicioException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error al comunicarse con otro microservicio: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
