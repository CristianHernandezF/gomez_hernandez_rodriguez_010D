package com.nubemedica.service_calendario.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //revisa cada fila de datos enviados a traves del controller y devuelve cual da el error
    @ExceptionHandler(MethodArgumentNotValidException.class) 
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>(); //diccionario q guarda los erros
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage()); //getField guardo la columna y getDefaultMessage guardo su mensaje de error
        }); //getBlindingResult -> toma los atributos de satelite, getFieldError -> obtiene en q falla, dsp recorro cada error y guardo cada error en el diccionario
        return ResponseEntity.badRequest().body(errores); //Mando el codigo http y dsp en el cuerpo mando los errores
    }

      // Recurso no encontrado (Paciente, Doctor, Evento)
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RecursoNoEncontradoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Conflicto de horario (Regla de negocio)
    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<Map<String, String>> handleHorarioNoDisponible(HorarioNoDisponibleException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Tipo de evento no encontrado en la BD
    @ExceptionHandler(TipoEventoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleTipoEventoNotFound(TipoEventoNoEncontradoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Error al consumir otro microservicio (WebClient)
    @ExceptionHandler(ComunicacionMicroservicioException.class)
    public ResponseEntity<Map<String, String>> handleComunicacionMicroservicio(ComunicacionMicroservicioException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error al comunicarse con otro microservicio: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // Captura genérica de WebClientResponseException (errores 4xx o 5xx de otros MS)
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientResponse(WebClientResponseException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "El microservicio externo respondió con error: " + ex.getStatusCode());
        error.put("detalle", ex.getResponseBodyAsString());
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // Cualquier otra excepción no controlada (caída general)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Ocurrió un error inesperado: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> handleAccesoDenegado(AccesoDenegadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Acceso prohibido: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error); // Retorna 403 Forbidden
    }

}

