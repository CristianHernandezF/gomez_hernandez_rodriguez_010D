package com.nubemedica.service_estadocita.service;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nubemedica.service_estadocita.dto.EstadoCitaMedicaDTO;
import com.nubemedica.service_estadocita.model.EstadoCita;
import com.nubemedica.service_estadocita.model.TipoEstadoCita;
import com.nubemedica.service_estadocita.repository.EstadoCitaRepository;
import com.nubemedica.service_estadocita.repository.TipoEstadoCitaRepository;

import jakarta.transaction.Transactional;

@Service
public class EstadoCitaService {


    @Autowired
    private EstadoCitaRepository estadoCitaRepository;
    @Autowired
    private TipoEstadoCitaRepository tipoEstadoCitaRepository;

    @Transactional
    public EstadoCita guardarEstadoCita(EstadoCitaMedicaDTO estadoCitaDTO){
        EstadoCita estadoCita = new EstadoCita();
        estadoCita.setObservacion(estadoCitaDTO.getObservaciones());
        estadoCita.setTipoEstadoCita(tipoEstadoCitaRepository.findByNombreEstado(estadoCitaDTO.getNombreEstado()));   
        return estadoCitaRepository.save(estadoCita);
    }

    public EstadoCitaMedicaDTO buscarPorId(Long id){
        EstadoCita estadoCita = estadoCitaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No existe ese Estado de cita"));
        
        return new EstadoCitaMedicaDTO(
            estadoCita.getTipoEstadoCita().getNombreEstado(),
            estadoCita.getObservacion()
        );    
    }

    public List<EstadoCita> BuscarTodasLasCitas(){
        return estadoCitaRepository.findAll();
    }


    public void EliminarCita(Long id){
        estadoCitaRepository.deleteById(id);
    }


    public String ModificaCita(EstadoCita estadocita){
        estadoCitaRepository.save(estadocita);
        return "Cita modificada";
    }

    
    @Transactional
    public void ModificaCitaPorNombre(Long idEstado, EstadoCitaMedicaDTO dto) {
            EstadoCita existente = estadoCitaRepository.findById(idEstado)
                .orElseThrow(() -> new RuntimeException("Estado de cita no encontrado"));

            // Buscamos el ID del tipo por nombre (Agendada, Completada, etc)
            TipoEstadoCita tipo = tipoEstadoCitaRepository.findByNombreEstado(dto.getNombreEstado());
            
            if (tipo != null) {
                existente.setTipoEstadoCita(tipo);}
            
            if (dto.getObservaciones() != null) {
                existente.setObservacion(dto.getObservaciones());}

            estadoCitaRepository.save(existente);
    }
}
