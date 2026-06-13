package com.nubemedica.service_notificaciones.service;

import com.nubemedica.service_notificaciones.dto.*;
import com.nubemedica.service_notificaciones.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_notificaciones.model.Notificacion;
import com.nubemedica.service_notificaciones.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository repository;

    // CREATE: Programar nueva notificación
    @Transactional
    public NotificacionResponseDTO programarNotificacion(NotificacionRequestDTO request) {
        Notificacion notificacion = new Notificacion();
        notificacion.setCorreoDestino(request.getCorreoDestino());
        notificacion.setAsunto(request.getAsunto());
        notificacion.setMensaje(request.getMensaje());
        notificacion.setFechaEnvio(request.getFechaEnvio());
        notificacion.setHoraEnvio(request.getHoraEnvio());
        notificacion.setEstadoEnvio(false); // Siempre inicia como pendiente

        return mapearAResponse(repository.save(notificacion));
    }

    // READ: Todas
    public List<NotificacionResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    // READ: Por ID
    public NotificacionResponseDTO obtenerPorId(Long id) {
        Notificacion notif = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));
        return mapearAResponse(notif);
    }

    // UPDATE: Manual (Por si se requiere corregir un mensaje o correo)
    @Transactional
    public NotificacionResponseDTO actualizarNotificacion(Long id, NotificacionRequestDTO request) {
        Notificacion notif = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));

        notif.setCorreoDestino(request.getCorreoDestino());
        notif.setAsunto(request.getAsunto());
        notif.setMensaje(request.getMensaje());
        notif.setFechaEnvio(request.getFechaEnvio());
        notif.setHoraEnvio(request.getHoraEnvio());

        return mapearAResponse(repository.save(notif));
    }

    // DELETE
    @Transactional
    public void eliminarNotificacion(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No se pudo eliminar: Notificación no existe");
        }
        repository.deleteById(id);
    }

    // MÉTODO EXTRA: Simular envío (Cambiar estado)
    @Transactional
    public void marcarComoEnviada(Long id) {
        Notificacion notif = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));
        notif.setEstadoEnvio(true);
        repository.save(notif);
    }

    private NotificacionResponseDTO mapearAResponse(Notificacion n) {
        return NotificacionResponseDTO.builder()
                .idNotificacion(n.getIdNotificacion())
                .correoDestino(n.getCorreoDestino())
                .asunto(n.getAsunto())
                .mensaje(n.getMensaje())
                .fechaEnvio(n.getFechaEnvio())
                .horaEnvio(n.getHoraEnvio())
                .estadoEnvio(n.isEstadoEnvio())
                .build();
    }
}