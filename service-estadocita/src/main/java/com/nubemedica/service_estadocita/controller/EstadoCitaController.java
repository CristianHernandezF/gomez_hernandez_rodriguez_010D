package com.nubemedica.service_estadocita.controller;



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

import com.nubemedica.service_estadocita.dto.EstadoCitaMedicaDTO;
import com.nubemedica.service_estadocita.model.EstadoCita;
import com.nubemedica.service_estadocita.model.TipoEstadoCita;
import com.nubemedica.service_estadocita.service.EstadoCitaService;
import com.nubemedica.service_estadocita.service.TipoEstadoCitaService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/v1/estadocita")
@CrossOrigin(origins = "http://localhost:8081")
public class EstadoCitaController {

    @Autowired
    private EstadoCitaService estadoCitaService;

    @Autowired
    private TipoEstadoCitaService tipoEstadoCitaService;

    @PostMapping
    @Operation(summary = "Crear un nuevo estado de cita", description = "Permite crear un nuevo estado de cita en el sistema utilizando los datos proporcionados en el cuerpo de la solicitud.")
    public Long crearEstadoCita(@RequestBody EstadoCitaMedicaDTO estadocita){
        EstadoCita estadocitado = estadoCitaService.guardarEstadoCita(estadocita);
        return estadocitado.getIdEstadoCita();

    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un estado de cita por su ID", description = "Devuelve los detalles de un estado de cita específico utilizando su ID.")
    public EstadoCitaMedicaDTO buscarEstadoCitaPorid(@PathVariable Long id){
            return estadoCitaService.buscarPorId(id);
    }

    @GetMapping("/todas")
    @Operation(summary = "Buscar todos los estados de cita", description = "Devuelve todos los estados de cita de la base de datos")
    public List<EstadoCita> buscarTodosLosEstadoCita(){
            return estadoCitaService.BuscarTodasLasCitas();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un estado de cita por su ID", description = "Elimina el estado de cita dado su ID de la base de datos")
    public ResponseEntity<String> EliminarEstadoCita(@PathVariable Long id) {
        estadoCitaService.EliminarCita(id);
        return ResponseEntity.ok("Se ha eliminado un estado de cita");
    }

    @PutMapping("/{id}")    
    @Operation(summary = "Actualizar un estado de cita por su ID", description = "Utilizando El ID y requiriendo un body se actualiza un estado de cita.")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id, @RequestBody EstadoCitaMedicaDTO dto) {
        estadoCitaService.ModificaCitaPorNombre(id, dto);
        return ResponseEntity.ok().build();
    }

    // Nuevo endpoint para buscar el ID del tipo de estado por su nombre
    @GetMapping("/tipo-id/{nombre}")
    @Operation(summary = "Obtener el ID de un tipo de estado por su nombre")
    public ResponseEntity<Long> obtenerIdTipoPorNombre(@PathVariable String nombre) {
        TipoEstadoCita tipo = tipoEstadoCitaService.EncontrarCitaPorNombre(nombre);
        if (tipo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tipo.getIdTipoEstado());
    }
    

}
