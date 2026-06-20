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

import com.hospital.service_direccion.model.Comuna;
import com.hospital.service_direccion.service.ComunaService;

import io.swagger.v3.oas.annotations.Operation;



@RestController
@RequestMapping("api/v1/comunas")
@CrossOrigin(origins = "http://localhost:8081")
public class ComunaController {

        @Autowired
        private ComunaService comunaService;


        @GetMapping("/todas")
        @Operation(summary = "Obtener todas las comunas", description = "Devuelve una lista de todas las comunas disponibles en el sistema.")
        public List<Comuna> listarTodas(){
            return comunaService.listarTodos();
        }
        
        @PostMapping
        @Operation(summary = "Crear una nueva comuna", description = "Permite crear una nueva comuna en el sistema utilizando los datos proporcionados en el cuerpo de la solicitud.")
        public ResponseEntity<Comuna> crear(@RequestBody Comuna comuna){
            return new ResponseEntity<>(comunaService.guardarComuna(comuna),HttpStatus.ACCEPTED);
        }
}
