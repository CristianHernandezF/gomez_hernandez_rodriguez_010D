package com.nubemedica.service_registropacientes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nubemedica.service_registropacientes.dto.PacienteRegistroResponse;
import com.nubemedica.service_registropacientes.dto.PacienteResponse;
import com.nubemedica.service_registropacientes.dto.PacienteResumenDTO;
import com.nubemedica.service_registropacientes.dto.PacienteRegistroRequest;
import com.nubemedica.service_registropacientes.dto.ActualizarPacienteRequest;
import com.nubemedica.service_registropacientes.service.PacienteService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("api/v1/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @PostMapping
    public ResponseEntity<PacienteRegistroResponse> registrarPaciente(@Valid @RequestBody PacienteRegistroRequest request, 
        @RequestHeader("X-Doctor-Run") String runDoctor){
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.guardarPaciente(request, runDoctor));
    }
    
    @GetMapping("/{runPaciente}")
    public ResponseEntity<PacienteResponse> obtenerPaciente (@PathVariable String runPaciente,
        @RequestHeader("X-Doctor-Run") String runDoctor,
        @RequestHeader("Authorization") String token){
        return ResponseEntity.ok(pacienteService.obtenerPacientePorRun(runPaciente, runDoctor));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listarMisPacientes(@RequestHeader("X-Doctor-Run") String runDoctor){
        return ResponseEntity.ok(pacienteService.listarPacientesDeUnDoctor(runDoctor));
    }

    @GetMapping("/{runPaciente}/resumen")
    public ResponseEntity<PacienteResumenDTO> obtenerResumen(@PathVariable String runPaciente) {
        return ResponseEntity.ok(pacienteService.obtenerResumenPaciente(runPaciente));
    }

    @PutMapping("/{runPaciente}")
    public ResponseEntity<PacienteResponse> actualizarPaciente(@PathVariable String runPaciente,
        @Valid @RequestBody ActualizarPacienteRequest request,
        @RequestHeader("X-Doctor-Run") String runDoctor){
        return ResponseEntity.ok(pacienteService.actualizarPaciente(runPaciente, request, runDoctor));
    }

    @DeleteMapping("/{runPaciente}/desasociar")
    public ResponseEntity<Void> desasociar(@PathVariable String runPaciente,
                                           @RequestHeader("X-Doctor-Run") String runDoctor) {
        pacienteService.eliminarRelacionDoctorPaciente(runPaciente, runDoctor);
        return ResponseEntity.noContent().build();
    }

}
