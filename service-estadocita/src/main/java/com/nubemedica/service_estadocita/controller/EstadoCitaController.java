package com.nubemedica.service_estadocita.controller;



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

import com.nubemedica.service_estadocita.dto.EstadoCitaMedicaDTO;
import com.nubemedica.service_estadocita.model.EstadoCita;
import com.nubemedica.service_estadocita.model.TipoEstadoCita;
import com.nubemedica.service_estadocita.service.EstadoCitaService;
import com.nubemedica.service_estadocita.service.TipoEstadoCitaService;

@RestController
@RequestMapping("api/v1/estadocita")
public class EstadoCitaController {

    @Autowired
    private EstadoCitaService estadoCitaService;

    @Autowired
    private TipoEstadoCitaService tipoEstadoCitaService;

    @PostMapping
    public Long crearEstadoCita(@RequestBody EstadoCitaMedicaDTO estadocita){
        EstadoCita estadocitado = estadoCitaService.guardarEstadoCita(estadocita);
        return estadocitado.getIdEstadoCita();

    }

    @GetMapping("/{id}")
    public EstadoCitaMedicaDTO buscarEstadoCitaPorid(@PathVariable Long id){
            return estadoCitaService.buscarPorId(id);
    }

    @GetMapping("/todas")
    public List<EstadoCita> buscarTodosLosEstadoCita(){
            return estadoCitaService.BuscarTodasLasCitas();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> EliminarEstadoCita(@PathVariable Long id) {
        estadoCitaService.EliminarCita(id);
        return ResponseEntity.ok("Se ha eliminado un estado de cita");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id, @RequestBody EstadoCitaMedicaDTO dto) {
        estadoCitaService.ModificaCitaPorNombre(id, dto);
        return ResponseEntity.ok().build();
    }

    // Nuevo endpoint para buscar el ID del tipo de estado por su nombre
    @GetMapping("/tipo-id/{nombre}")
    public ResponseEntity<Long> obtenerIdTipoPorNombre(@PathVariable String nombre) {
        TipoEstadoCita tipo = tipoEstadoCitaService.EncontrarCitaPorNombre(nombre);
        if (tipo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tipo.getIdTipoEstado());
    }
    

}
