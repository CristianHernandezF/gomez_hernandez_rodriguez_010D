package com.hospital.service_direccion.service;

import com.hospital.service_direccion.dto.DireccionRequest;
import com.hospital.service_direccion.dto.DireccionResponse;
import com.hospital.service_direccion.model.*;
import com.hospital.service_direccion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;
    @Autowired
    private ComunaRepository comunaRepository;
    @Autowired
    private RegionRepository regionRepository;

    public List<DireccionResponse> listarTodos() {
        return direccionRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DireccionResponse obtenerPorId(Long id) {
        Direccion dir = direccionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no Existe"));
        return mapearADTO(dir);
    }

    @Transactional
    public DireccionResponse crearDireccion(DireccionRequest dto) {
        // 1. Buscar Región por nombre
        Region region = regionRepository.findByNombre(dto.getRegion())
                .orElseThrow(() -> new RuntimeException("Región no encontrada: " + dto.getRegion()));

        // 2. Buscar Comuna por nombre dentro de esa región
        Comuna comuna = comunaRepository.findByNombreAndRegion(dto.getComuna(), region)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada en esa región"));

        Direccion direccion = new Direccion();
        direccion.setNombre(dto.getNombre());
        direccion.setComuna(comuna);
        direccionRepository.save(direccion);
        
        return mapearADTO(direccion);
    }

    @Transactional
    public void actualizarDireccion(Long id, DireccionRequest dto) {
        Direccion existente = direccionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));

        Region region = regionRepository.findByNombre(dto.getRegion())
                .orElseThrow(() -> new RuntimeException("Región no encontrada"));

        Comuna comuna = comunaRepository.findByNombreAndRegion(dto.getComuna(), region)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada"));

        existente.setNombre(dto.getNombre());
        existente.setComuna(comuna);

        direccionRepository.save(existente);
    }

    @Transactional
    public void eliminarDireccion(Long id) {
        if (!direccionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada");
        }
        direccionRepository.deleteById(id);
    }

    // Método de apoyo para convertir Entidad compleja a DTO Plano
    private DireccionResponse mapearADTO(Direccion dir) {
        return DireccionResponse.builder()
                .idDireccion(dir.getId())
                .nombre(dir.getNombre())
                .comuna(dir.getComuna().getNombre())
                .region(dir.getComuna().getRegion().getNombre())
                .build();
    }
}