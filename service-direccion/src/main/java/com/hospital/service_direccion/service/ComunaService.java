package com.hospital.service_direccion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.service_direccion.model.Comuna;
import com.hospital.service_direccion.model.Region;
import com.hospital.service_direccion.repository.ComunaRepository;
import com.hospital.service_direccion.repository.RegionRepository;

@Service
public class ComunaService {
    @Autowired
    private ComunaRepository comunaRepository;
    @Autowired
    private RegionRepository regionRepository;

    public List<Comuna> listarTodos(){
        return comunaRepository.findAll();
    }


    public Comuna guardarComuna(Comuna comuna){
        Long regionId = comuna.getRegion().getId();
        Region region = regionRepository.findById(regionId)
                        .orElseThrow(() -> new RuntimeException("Region no encontrada"+regionId));
        comuna.setRegion(region);
        return comunaRepository.save(comuna);
    }

}
