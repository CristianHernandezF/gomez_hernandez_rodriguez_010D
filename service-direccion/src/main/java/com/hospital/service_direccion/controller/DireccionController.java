package com.hospital.service_direccion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.hospital.service_direccion.dto.DireccionDTO;
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
        public Long crear(@RequestBody DireccionDTO direccionDTO){
            
            Direccion nuevaDireccion = direccionService.creaDireccion(direccionDTO);
            return nuevaDireccion.getId();
        }

        @PutMapping("/{id}")
        public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody DireccionDTO direccionDTO) {
            direccionService.actualizarDireccion(id, direccionDTO);
            return ResponseEntity.ok().build();
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(@PathVariable Long id) {
            direccionService.eliminarDireccion(id);
            return ResponseEntity.noContent().build();
        }
        
}
