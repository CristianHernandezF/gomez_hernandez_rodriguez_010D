package com.hospital.service_direccion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.service_direccion.model.Region;
import com.hospital.service_direccion.repository.RegionRepository;

@Service
public class RegionService {
    
    @Autowired
    private RegionRepository regionRepository;

    public List<Region> listarTodos(){
        return regionRepository.findAll();
    }

    public Region guardar(Region region){
        return regionRepository.save(region);
    }
}
