package com.hospital.service_direccion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hospital.service_direccion.model.Comuna;
import com.hospital.service_direccion.model.Direccion;
import com.hospital.service_direccion.repository.ComunaRepository;
import com.hospital.service_direccion.repository.DireccionRepository;

import jakarta.transaction.Transactional;


@Service
public class DireccionService {
        @Autowired
        private DireccionRepository direccionRepository;
        private ComunaRepository comunaRepository;



        public List<Direccion> listarTodos(){
            return direccionRepository.findAll();
        }

        @Transactional
        public Direccion obtenerPorId(Long id) {
                return direccionRepository.findById(id)
                                          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Dirección no Existe"));
    }

        public Direccion creaDireccion(Direccion direccion){

            Long comunaId = direccion.getComuna().getId();

            Comuna comuna = comunaRepository.findById(comunaId)
                            .orElseThrow(() -> new RuntimeException("Comuna no encontrada"));
            

            direccion.setComuna(comuna);
            return direccionRepository.save(direccion);
            
        }
}
