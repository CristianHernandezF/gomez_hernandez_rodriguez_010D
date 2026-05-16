package com.nubemedica.service_estadocita.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nubemedica.service_estadocita.model.TipoEstadoCita;
import com.nubemedica.service_estadocita.repository.TipoEstadoCitaRepository;

@Service
public class TipoEstadoCitaService {

    @Autowired
    private TipoEstadoCitaRepository tipoEstadoCitaRepository;
    
    public TipoEstadoCita guardar(TipoEstadoCita tipoEstadoCita){
        return tipoEstadoCitaRepository.save(tipoEstadoCita);
    }

    public TipoEstadoCita EncontrarCitaPorNombre(String nombre){
        return tipoEstadoCitaRepository.findByNombreEstado(nombre);
    }

}
