package com.nubemedica.service_registropacientes.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //revisa cada fila de datos enviados a traves del controller y devuelve cual da el error
    @ExceptionHandler(MethodArgumentNotValidException.class) 
    public ResponseEntity<Map<String, String>> handleDatosPacientes(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>(); //diccionario q guarda los erros
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage()); //getField guardo la columna y getDefaultMessage guardo su mensaje de error
        }); //getBlindingResult -> toma los atributos de satelite, getFieldError -> obtiene en q falla, dsp recorro cada error y guardo cada error en el diccionario
        return ResponseEntity.badRequest().body(errores); //Mando el codigo http y dsp en el cuerpo mando los errores
    }

    @ExceptionHandler(NoExistePacienteException.class)
    public ResponseEntity<Map<String ,String>> handleExistePaciente(NoExistePacienteException ex) {
        Map<String, String> error = new HashMap<>();

        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error); //404
    }
    
    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleDatoDuplicado(DatoDuplicadoException ex) {
        Map<String, String> error = new HashMap<>();
        
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // 409 Conflict
    }

    @ExceptionHandler(ComunicacionMicroservicioException.class)
    public ResponseEntity<Map<String, String>> handleComunicacionMicroservicio(ComunicacionMicroservicioException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error al comunicarse con otro microservicio: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> handleComunicacionMicroservicio(AccesoDenegadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
