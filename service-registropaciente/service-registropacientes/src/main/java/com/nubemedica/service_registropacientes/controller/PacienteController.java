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

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("api/v1/pacientes")
@CrossOrigin(origins = "http://localhost:8081")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @PostMapping
    @Operation(summary = "Crear un paciente",description = "Requiere datos del paciente y rut del doctor")
    public ResponseEntity<PacienteRegistroResponse> registrarPaciente(@Valid @RequestBody PacienteRegistroRequest request, 
        @RequestHeader("X-Doctor-Run") String runDoctor){
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.guardarPaciente(request, runDoctor));
    }
    
    @GetMapping("/{runPaciente}")
    @Operation(summary = "Obtener un paciente",description = "Requiere el token,el rut del paciente y rut del doctor")
    public ResponseEntity<PacienteResponse> obtenerPaciente (@PathVariable String runPaciente,
        @RequestHeader("X-Doctor-Run") String runDoctor,
        @RequestHeader("Authorization") String token){
        return ResponseEntity.ok(pacienteService.obtenerPacientePorRun(runPaciente, runDoctor));
    }

    @GetMapping
    @Operation(summary = "Lista paciente de un doctor",description = "requiere autentificacion y rut del doctor")
    public ResponseEntity<List<PacienteResponse>> listarMisPacientes(@RequestHeader("X-Doctor-Run") String runDoctor){
        return ResponseEntity.ok(pacienteService.listarPacientesDeUnDoctor(runDoctor));
    }

    @GetMapping("/{runPaciente}/resumen")
    @Operation(summary = "Obtener un resumen de datos del paciente",description = "se requiere un rut de paciente")
    public ResponseEntity<PacienteResumenDTO> obtenerResumen(@PathVariable String runPaciente) {
        return ResponseEntity.ok(pacienteService.obtenerResumenPaciente(runPaciente));
    }

    @PutMapping("/{runPaciente}")
    @Operation(summary = "Actualizar paciente",description = "Se requiere autentificacion del doctor, rut del doctor y rut del paciente")
    public ResponseEntity<PacienteResponse> actualizarPaciente(@PathVariable String runPaciente,
        @Valid @RequestBody ActualizarPacienteRequest request,
        @RequestHeader("X-Doctor-Run") String runDoctor){
        return ResponseEntity.ok(pacienteService.actualizarPaciente(runPaciente, request, runDoctor));
    }

    @DeleteMapping("/{runPaciente}/desasociar")
    @Operation(summary = "Desasociar Paciente",description = "Solamente se desasocia el paciente debido a que mas doctores podrian tener este paciente, se necesita el rut del paciente ,autentificacion y rut de doctor")
    public ResponseEntity<Void> desasociar(@PathVariable String runPaciente,
                                           @RequestHeader("X-Doctor-Run") String runDoctor) {
        pacienteService.eliminarRelacionDoctorPaciente(runPaciente, runDoctor);
        return ResponseEntity.noContent().build();
    }

}
