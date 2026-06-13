package com.nubemedica.service_telemedicina.controller;

import com.nubemedica.service_telemedicina.dto.TelemedicinaResponse;
import com.nubemedica.service_telemedicina.service.TelemedicinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemedicina")
public class TelemedicinaController {

    @Autowired
    private TelemedicinaService telemedicinaService;

    // GENERAR SESIÓN (Utilizado principalmente por MS-Calendario)
    @PostMapping("/generar")
    public ResponseEntity<TelemedicinaResponse> crearSesionAutomatica() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(telemedicinaService.generarSesionAutomatica());
    }

    // LISTAR TODAS LAS SESIONES
    @GetMapping
    public ResponseEntity<List<TelemedicinaResponse>> listarSesiones() {
        return ResponseEntity.ok(telemedicinaService.listarTodos());
    }

    // OBTENER UNA SESIÓN POR ID
    @GetMapping("/{id}")
    public ResponseEntity<TelemedicinaResponse> obtenerSesion(@PathVariable Long id) {
        return ResponseEntity.ok(telemedicinaService.obtenerPorId(id));
    }

    // REGENERAR SESIÓN (Actualiza link y código automáticamente)
    @PutMapping("/{id}")
    public ResponseEntity<TelemedicinaResponse> regenerarSesion(@PathVariable Long id) {
        return ResponseEntity.ok(telemedicinaService.regenerarSesion(id));
    }

    // ELIMINAR SESIÓN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSesion(@PathVariable Long id) {
        telemedicinaService.eliminarSesion(id);
        return ResponseEntity.noContent().build();
    }
}