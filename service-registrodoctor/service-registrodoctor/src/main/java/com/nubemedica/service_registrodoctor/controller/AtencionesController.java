package com.nubemedica.service_registrodoctor.controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nubemedica.service_registrodoctor.dto.AtencionesDoctorDTO;
import com.nubemedica.service_registrodoctor.model.AtencionesDoctor;
import com.nubemedica.service_registrodoctor.service.AtencionesDoctorService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/atenciones")
@CrossOrigin(origins = "http://localhost:8081")
public class AtencionesController {

    @Autowired
    private AtencionesDoctorService AtencionesDoctorService;

    @PostMapping()
    @Operation(summary = "Registrar atencion de un doctor",description = "Requiere un body con el rut del paciente y el rut del doctor")
    public ResponseEntity<AtencionesDoctor> registrarAtencion(@RequestBody AtencionesDoctorDTO atencionDTO) {
        return ResponseEntity.ok(AtencionesDoctorService.registrarAtencion(atencionDTO));
    }

    @GetMapping("/doctor")
    @Operation(summary = "Lista todos los pacientes pertenecientes a un doctor por su RUT")
    public List<AtencionesDoctor> listarPacientesDeDoctor(@RequestHeader("X-Doctor-Run") String runDoctor) {
        return AtencionesDoctorService.listarPacientesDeDoctor(runDoctor);
    }

    @GetMapping("paciente/{runPaciente}")
    @Operation(summary = "Lista el paciente en especifico del doctor por el rut del paciente")
    public List<AtencionesDoctor> listarDoctoresDePaciente(@PathVariable String runPaciente) {
        return AtencionesDoctorService.listarDoctoresDePaciente(runPaciente);
    }

    @GetMapping("doctor/{runDoctor}")
    @Operation(summary = "Lista solo los rut de los pacientes pertenecientes a un doctor por su Rut")
    public List<String> listarRutsPacientesDeDoctor(@PathVariable String runDoctor) {
        return AtencionesDoctorService.RutsPacientesDoctor(runDoctor);
    }

    @DeleteMapping("/{runPaciente}/desasociar")
    @Operation(summary = "Elimina la relacion entre paciente y doctor",description = "Requiere el rut del doctor y paciente")
    public ResponseEntity<Void> eliminarRelacion(
            @PathVariable String runPaciente, 
            @RequestBody String runDoctor) { // Cambiado a @RequestBody para capturar lo que envía el MS-PACIENTE
        AtencionesDoctorService.eliminarRelacion(runPaciente, runDoctor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/existe/{runDoctor}/{runPaciente}")
    @Operation(summary = "Verifica si existe una relacion entre doctor y un paciente",description = "se necesita el rut paciente y doctor")
    public boolean getMethodName(@PathVariable String runDoctor, @PathVariable String runPaciente) {
        return AtencionesDoctorService.verificarRelacion(runDoctor, runPaciente);     
    }

}
