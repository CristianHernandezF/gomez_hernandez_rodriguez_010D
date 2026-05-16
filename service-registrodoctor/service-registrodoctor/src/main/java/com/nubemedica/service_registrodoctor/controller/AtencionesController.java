package com.nubemedica.service_registrodoctor.controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/atenciones")
public class AtencionesController {

    @Autowired
    private AtencionesDoctorService AtencionesDoctorService;

    @PostMapping()
    public ResponseEntity<AtencionesDoctor> registrarAtencion(@RequestBody AtencionesDoctorDTO atencionDTO) {
        return ResponseEntity.ok(AtencionesDoctorService.registrarAtencion(atencionDTO));
    }

    @GetMapping("/doctor")
    public List<AtencionesDoctor> listarPacientesDeDoctor(@RequestHeader("X-Doctor-Run") String runDoctor) {
        return AtencionesDoctorService.listarPacientesDeDoctor(runDoctor);
    }

    @GetMapping("paciente/{runPaciente}")
    public List<AtencionesDoctor> listarDoctoresDePaciente(@PathVariable String runPaciente) {
        return AtencionesDoctorService.listarDoctoresDePaciente(runPaciente);
    }

    @GetMapping("doctor/{runDoctor}")
    public List<String> listarRutsPacientesDeDoctor(@PathVariable String runDoctor) {
        return AtencionesDoctorService.RutsPacientesDoctor(runDoctor);
    }

    @DeleteMapping("/{runPaciente}/desasociar")
    public ResponseEntity<Void> eliminarRelacion(
            @PathVariable String runPaciente, 
            @RequestBody String runDoctor) { // Cambiado a @RequestBody para capturar lo que envía el MS-PACIENTE
        AtencionesDoctorService.eliminarRelacion(runPaciente, runDoctor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/existe/{runDoctor}/{runPaciente}")
    public boolean getMethodName(@PathVariable String runDoctor, @PathVariable String runPaciente) {
        return AtencionesDoctorService.verificarRelacion(runDoctor, runPaciente);     
    }

}
