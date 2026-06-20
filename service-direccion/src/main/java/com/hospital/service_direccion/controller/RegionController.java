package com.hospital.service_direccion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.service_direccion.model.Region;
import com.hospital.service_direccion.service.RegionService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/regiones")
@CrossOrigin(origins = "http://localhost:8081")
public class RegionController {

        @Autowired
        private RegionService regionService;

        @GetMapping("/todas")
        @Operation(summary = "Obtener todas las regiones", description = "Devuelve una lista de todas las regiones disponibles en el sistema.")
        public List<Region> listarTodas(){
            return regionService.listarTodos();
        }

        @PostMapping
        @Operation(summary = "Crear nueva región", description = "Permite crear una nueva región en el sistema utilizando los datos proporcionados en el cuerpo de la solicitud.")
        public ResponseEntity<Region> crear(@RequestBody Region region){
            return new ResponseEntity<>(regionService.guardar(region),HttpStatus.CREATED);
        }
}
