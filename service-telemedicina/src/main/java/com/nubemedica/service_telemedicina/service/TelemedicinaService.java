package com.nubemedica.service_telemedicina.service;

import com.nubemedica.service_telemedicina.dto.*;
import com.nubemedica.service_telemedicina.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_telemedicina.model.SesionTelemedicina;
import com.nubemedica.service_telemedicina.repository.SesionTelemedicinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Random;

@Service
public class TelemedicinaService {

    @Autowired
    private SesionTelemedicinaRepository repository;

    // CREATE (Generación automática para el Calendario)
    @Transactional
    public TelemedicinaResponse generarSesionAutomatica() {
        SesionTelemedicina sesion = new SesionTelemedicina();
        aplicarNuevosDatos(sesion); // Usamos la lógica de generación
        return mapearAResponse(repository.save(sesion));
    }

    // READ (Todos)
    public List<TelemedicinaResponse> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    // READ (Por ID)
    public TelemedicinaResponse obtenerPorId(Long id) {
        SesionTelemedicina sesion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada con ID: " + id));
        return mapearAResponse(sesion);
    }

    // UPDATE
    @Transactional
    public TelemedicinaResponse regenerarSesion(Long id) {
        SesionTelemedicina sesion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada con ID: " + id));
        
        aplicarNuevosDatos(sesion); // Sobrescribimos con datos nuevos
        
        return mapearAResponse(repository.save(sesion));
    }
    // DELETE
    @Transactional
    public void eliminarSesion(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No se puede eliminar: Sesión no encontrada");
        }
        repository.deleteById(id);
    }

    // Métodos privados de apoyo
    private String generarCodigoAleatorio(int tamano) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < tamano) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    private void aplicarNuevosDatos(SesionTelemedicina sesion) {
        String nuevoLink = "https://nube-medica.com/teleconsulta/" + UUID.randomUUID().toString().substring(0, 8);
        String nuevoCodigo = generarCodigoAleatorio(6);
        
        sesion.setLinkAcceso(nuevoLink);
        sesion.setCodigoAcceso(nuevoCodigo);
    }

    private TelemedicinaResponse mapearAResponse(SesionTelemedicina sesion) {
        return new TelemedicinaResponse(
                sesion.getIdSesionTelemedicina(), 
                sesion.getLinkAcceso(),
                sesion.getCodigoAcceso()
        );
    }
}