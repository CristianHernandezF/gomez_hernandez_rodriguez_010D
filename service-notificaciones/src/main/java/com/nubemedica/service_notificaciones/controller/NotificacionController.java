package com.nubemedica.service_notificaciones.controller;

import com.nubemedica.service_notificaciones.dto.*;
import com.nubemedica.service_notificaciones.service.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@CrossOrigin(origins = "http://localhost:8081")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    // Programar (Llamado por MS-Calendario)
    @PostMapping
    @Operation(summary = "Crear una notificacion",description = "requiere body de notificacion")
    public ResponseEntity<NotificacionResponseDTO> crear(@Valid @RequestBody NotificacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.programarNotificacion(request));
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones")
    public ResponseEntity<List<NotificacionResponseDTO>> listar() {
        return ResponseEntity.ok(notificacionService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificacion por su ID")
    public ResponseEntity<NotificacionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una notificacion por su ID")
    public ResponseEntity<NotificacionResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody NotificacionRequestDTO request) {
        return ResponseEntity.ok(notificacionService.actualizarNotificacion(id, request));
    }

    @PatchMapping("/{id}/enviar")
    @Operation(summary = "Cambiar estado a enviado de un notificacion por ID")
    public ResponseEntity<Void> marcarEnviada(@PathVariable Long id) {
        notificacionService.marcarComoEnviada(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificacion por ID")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}
