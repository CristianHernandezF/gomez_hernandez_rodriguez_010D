package com.hospital.service_direccion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hospital.service_direccion.dto.DireccionDTO;
import com.hospital.service_direccion.model.Comuna;
import com.hospital.service_direccion.model.Direccion;
import com.hospital.service_direccion.repository.ComunaRepository;
import com.hospital.service_direccion.repository.DireccionRepository;

import jakarta.transaction.Transactional;


@Service
public class DireccionService {
        @Autowired
        private DireccionRepository direccionRepository;
        @Autowired
        private ComunaRepository comunaRepository;



        public List<Direccion> listarTodos(){
            return direccionRepository.findAll();
        }

        @Transactional
        public Direccion obtenerPorId(Long id) {
                return direccionRepository.findById(id)
                                          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Dirección no Existe"));
    }

        public Direccion creaDireccion(DireccionDTO direccionDTO){

            Long comunaId = direccionDTO.getComunaId();

            Comuna comuna = comunaRepository.findById(comunaId)
                            .orElseThrow(() -> new RuntimeException("Comuna no encontrada"));
            
            Direccion direccion = new Direccion();
            
            direccion.setComuna(comuna);
            direccion.setNombre(direccionDTO.getNombre());
            return direccionRepository.save(direccion);
            
        }

        @Transactional
        public void actualizarDireccion(Long id, DireccionDTO direccionDTO) {
            Direccion direccionExistente = direccionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));

            Long comunaId = direccionDTO.getComunaId();
            Comuna comuna = comunaRepository.findById(comunaId)
                    .orElseThrow(() -> new RuntimeException("Comuna no encontrada"));

            direccionExistente.setComuna(comuna);
            direccionExistente.setNombre(direccionDTO.getNombre());

            direccionRepository.save(direccionExistente);
        }

        @Transactional
        public void eliminarDireccion(Long id) {
            if (!direccionRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada");
            }
            direccionRepository.deleteById(id);
        }
}
