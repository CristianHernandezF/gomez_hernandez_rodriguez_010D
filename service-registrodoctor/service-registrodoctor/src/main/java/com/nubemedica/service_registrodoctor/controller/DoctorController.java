package com.nubemedica.service_registrodoctor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nubemedica.service_registrodoctor.dto.ActualizarDoctorRequest;
import com.nubemedica.service_registrodoctor.dto.DoctorResponse;
import com.nubemedica.service_registrodoctor.dto.RegistrarDoctorRequest;
import com.nubemedica.service_registrodoctor.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/doctores")
@CrossOrigin(origins = "http://localhost:8081")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SECCIÓN DOCTOR
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @PostMapping("")
    @Operation(summary = "Crea un doctor a la base de datos",description = "Utiliza el requestbody para crear un doctor IMPORTANTE LONGITUD DE CONTRASEÑA 8 CARACTERES")
    public ResponseEntity<DoctorResponse> registrarDoctor(@Valid @RequestBody RegistrarDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.registrarDoctor(request));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos los doctores si se tiene un rol de Admin")
    public List<DoctorResponse> listarDoctores() {
        return doctorService.listarTodos();
    }

    @GetMapping("/{runDoctor}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') and #runDoctor == authentication.name")
    @Operation(summary = "Se obtiene informacion del doctor por RUT",description = "Solo puede obtener informacion de si mismo.")
    public ResponseEntity<DoctorResponse> obtenerDoctor(@PathVariable String runDoctor) {
        return ResponseEntity.ok(doctorService.obtenerPorRun(runDoctor));
    }

    @PutMapping("/{runDoctor}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') and #runDoctor == authentication.name")
    @Operation(summary = "Actualizar informacion de un doctor utilizando su rut")
    public ResponseEntity<DoctorResponse> actualizarDoctor(
            @PathVariable String runDoctor,
            @Valid @RequestBody ActualizarDoctorRequest request) {
        return ResponseEntity.ok(doctorService.actualizarDoctor(runDoctor, request));
    }

    @DeleteMapping("/{runDoctor}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') and #runDoctor == authentication.name")
    @Operation(summary = "Eliminar a un doctor por RUT", description = "Solo puede eliminarse el mismo")
    public ResponseEntity<Void> eliminarDoctor(@PathVariable String runDoctor) {
        doctorService.eliminarDoctor(runDoctor);
        return ResponseEntity.noContent().build();
    }
    
}
