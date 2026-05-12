package com.hospital.service_direccion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.hospital.service_direccion.model.Direccion;
import com.hospital.service_direccion.service.DireccionService;

@RestController
@RequestMapping("api/v1/direcciones")
public class DireccionController {
        @Autowired
        private DireccionService direccionService;

        @GetMapping
        public List<Direccion> listarTodas(){
            return direccionService.listarTodos();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Direccion> obtenerPorId(@PathVariable Long id) {
            return ResponseEntity.ok(direccionService.obtenerPorId(id));
        }

        @PostMapping
        public Long crear(@RequestBody Direccion direccion){
            
            Direccion nuevaDireccion = direccionService.creaDireccion(direccion);
            return nuevaDireccion.getId();
        }
}
