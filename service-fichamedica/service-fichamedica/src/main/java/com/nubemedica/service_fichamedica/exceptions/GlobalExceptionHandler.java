package com.nubemedica.service_fichamedica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de VALIDACIONES (DTOs)
    // Captura errores de @NotBlank, @Min, @Email, etc.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    // 2. Recurso No Encontrado (404)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RecursoNoEncontradoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 3. Acceso Denegado / Prohibido (403)
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccesoDenegadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Acceso prohibido: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 4. Datos Duplicados (409 Conflict)
    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DatoDuplicadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 5. Error en comunicación con Microservicios Externos (503)
    @ExceptionHandler(ComunicacionMicroservicioException.class)
    public ResponseEntity<Map<String, String>> handleExternalCommunication(ComunicacionMicroservicioException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Falla en microservicio externo: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // 6. Captura genérica de errores de WebClient (4xx o 5xx de otros MS)
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientResponse(WebClientResponseException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "El microservicio externo respondió con error: " + ex.getStatusCode());
        error.put("detalle", ex.getResponseBodyAsString());
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // 7. Cualquier otra excepción no controlada (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Ocurrió un error inesperado en el servidor");
        error.put("detalle", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
