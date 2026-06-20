package com.hospital.service_direccion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.service_direccion.dto.DireccionRequest;
import com.hospital.service_direccion.dto.DireccionResponse;
import com.hospital.service_direccion.service.DireccionService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/v1/direcciones")
@CrossOrigin(origins = "http://localhost:8081")
public class DireccionController {
        @Autowired
        private DireccionService direccionService;

        @GetMapping
        @Operation(summary = "Listar todas las direcciones", description = "Devuelve una lista de todas las direcciones registradas en el sistema." )
        public List<DireccionResponse> listarTodas(){
            return direccionService.listarTodos();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener dirección por ID", description = "Devuelve los detalles de una dirección específica utilizando su ID." )
        public ResponseEntity<DireccionResponse> obtenerPorId(@PathVariable Long id) {
            return ResponseEntity.ok(direccionService.obtenerPorId(id));
        }

        @PostMapping
        @Operation(summary = "Crear nueva dirección", description = "Permite crear una nueva dirección en el sistema utilizando los datos proporcionados en el cuerpo de la solicitud." )
        public DireccionResponse crear(@RequestBody DireccionRequest direccionRequest){
            
            DireccionResponse nuevaDireccion = direccionService.crearDireccion(direccionRequest);
            return nuevaDireccion;
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar dirección existente", description = "Permite actualizar los detalles de una dirección existente utilizando su ID y los datos proporcionados en el cuerpo de la solicitud." ) 
        public ResponseEntity<Void> actualizar(@PathVariable Long id, 
            @RequestBody DireccionRequest direccionRequest) {
            direccionService.actualizarDireccion(id, direccionRequest);
            return ResponseEntity.ok().build();
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar dirección", description = "Permite eliminar una dirección específica utilizando su ID." )
        public ResponseEntity<Void> eliminar(@PathVariable Long id) {
            direccionService.eliminarDireccion(id);
            return ResponseEntity.noContent().build();
        }
        
}
