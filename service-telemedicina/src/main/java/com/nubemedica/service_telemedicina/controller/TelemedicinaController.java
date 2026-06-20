package com.nubemedica.service_telemedicina.controller;

import com.nubemedica.service_telemedicina.dto.TelemedicinaResponse;
import com.nubemedica.service_telemedicina.service.TelemedicinaService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemedicina")
@CrossOrigin(origins = "http://localhost:8081")
public class TelemedicinaController {

    @Autowired
    private TelemedicinaService telemedicinaService;

    // GENERAR SESIÓN (Utilizado principalmente por MS-Calendario)
    @PostMapping("/generar")
    @Operation(summary = "Generar un link de telemedicina")
    public ResponseEntity<TelemedicinaResponse> crearSesionAutomatica() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(telemedicinaService.generarSesionAutomatica());
    }

    // LISTAR TODAS LAS SESIONES
    @GetMapping
    @Operation(summary = "Obtener todos los link de sesiones de telemedicina")
    public ResponseEntity<List<TelemedicinaResponse>> listarSesiones() {
        return ResponseEntity.ok(telemedicinaService.listarTodos());
    }

    // OBTENER UNA SESIÓN POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener link de telemedicina por su ID")
    public ResponseEntity<TelemedicinaResponse> obtenerSesion(@PathVariable Long id) {
        return ResponseEntity.ok(telemedicinaService.obtenerPorId(id));
    }

    // REGENERAR SESIÓN (Actualiza link y código automáticamente)
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar link de telemedicina")
    public ResponseEntity<TelemedicinaResponse> regenerarSesion(@PathVariable Long id) {
        return ResponseEntity.ok(telemedicinaService.regenerarSesion(id));
    }

    // ELIMINAR SESIÓN
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sesion de telemedicina")
    public ResponseEntity<Void> eliminarSesion(@PathVariable Long id) {
        telemedicinaService.eliminarSesion(id);
        return ResponseEntity.noContent().build();
    }
}