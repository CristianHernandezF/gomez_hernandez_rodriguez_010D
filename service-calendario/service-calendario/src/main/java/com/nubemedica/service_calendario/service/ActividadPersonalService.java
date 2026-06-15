package com.nubemedica.service_calendario.service;

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.exceptions.*;
import com.nubemedica.service_calendario.model.*;
import com.nubemedica.service_calendario.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ActividadPersonalService {

    @Autowired
    private ActividadPersonalRepository actividadPersonalRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TipoEventoRepository tipoEventoRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${ms.doctores.url:http://localhost:8085/api/v1/doctores}")
    private String urlDoctores;

    @Value("${ms.notificaciones.url:http://localhost:8089/api/v1/notificaciones}")
    private String urlNotificaciones;

    // ======================================
    // MÉTODOS PÚBLICOS (LÓGICA DE NEGOCIO)
    // ======================================

    @Transactional
    public ActividadPersonalResponseDTO crearActividadPersonal(ActividadPersonalRequestDTO request, String runDoctor) {
        validarFecha(request.getFecha());
        validarTraslapeHorario(runDoctor, request.getFecha(), request.getHora());

        TipoEvento tipo = tipoEventoRepository.findByNombreTipo("Actividad Personal");
        if (tipo == null) throw new TipoEventoNoEncontradoException("Tipo 'Actividad Personal' no configurado");

        ActividadPersonal actividad = new ActividadPersonal();
        actividad.setFecha(request.getFecha());
        actividad.setHora(request.getHora());
        actividad.setRunDoctor(runDoctor);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setTipoEvento(tipo);

        ActividadPersonal guardada = actividadPersonalRepository.save(actividad);

        // Flujo: Programar notificación de recordatorio para el doctor
        programarNotificacionActividad(guardada, "RECORDATORIO");

        return mapearAResponse(guardada);
    }

    @Transactional
    public ActividadPersonalResponseDTO actualizarActividadPersonal(Long id, ActividadPersonalRequestDTO request, String runDoctorToken) {
        ActividadPersonal actividad = actividadPersonalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad personal no encontrada"));

        validarPropiedad(actividad, runDoctorToken);

        if (!actividad.getFecha().equals(request.getFecha()) || !actividad.getHora().equals(request.getHora())) {
            validarFecha(request.getFecha());
            validarTraslapeHorario(runDoctorToken, request.getFecha(), request.getHora());
        }

        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setFecha(request.getFecha());
        actividad.setHora(request.getHora());

        ActividadPersonal actualizada = actividadPersonalRepository.save(actividad);
        
        // Flujo: Actualizar la notificación con los nuevos datos
        programarNotificacionActividad(actualizada, "ACTUALIZACIÓN DE ACTIVIDAD");

        return mapearAResponse(actualizada);
    }

    @Transactional
    public void eliminarActividadPersonal(Long id, String runDoctorToken) {
        ActividadPersonal actividad = actividadPersonalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no existe"));

        validarPropiedad(actividad, runDoctorToken);

        // Flujo: Eliminar notificaciones pendientes asociadas a este evento antes de borrarlo
        eliminarNotificacionesPorEvento(id);

        actividadPersonalRepository.deleteById(id);
    }

    public ActividadPersonalResponseDTO obtenerActividadPersonalPorId(Long id, String runDoctorToken) {
        ActividadPersonal actividad = actividadPersonalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad personal no encontrada"));

        validarPropiedad(actividad, runDoctorToken);
        return mapearAResponse(actividad);
    }

    public List<ActividadPersonalResponseDTO> listarActividadesPorDoctor(String runDoctorToken) {
        return actividadPersonalRepository.findByRunDoctor(runDoctorToken).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    private void programarNotificacionActividad(ActividadPersonal actividad, String accion) {
        // Regla: 1 día antes a las 18:00
        LocalDate fechaEnvio = actividad.getFecha().minusDays(1);
        if (fechaEnvio.isBefore(LocalDate.now())) {
            fechaEnvio = LocalDate.now();
        }

        DoctorResponseDTO doc = obtenerDatosDoctor(actividad.getRunDoctor());

        NotificacionRequestDTO notif = new NotificacionRequestDTO();
        notif.setCorreoDestino(doc.getCorreo());
        notif.setAsunto(accion + ": " + actividad.getNombreActividad());
        notif.setMensaje("Usted tiene programada la actividad: " + actividad.getNombreActividad() + 
                         ". Descripción: " + actividad.getDescripcion() + 
                         ". Programada para el: " + actividad.getFecha() + " a las " + actividad.getHora());
        notif.setFechaEnvio(fechaEnvio);
        notif.setHoraEnvio(LocalTime.of(18, 0));
        notif.setIdEvento(actividad.getIdEvento());

        enviarANotificaciones(notif);
    }

    // ======================================
    // VALIDACIONES
    // ======================================

    private void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten fechas pasadas para actividades personales.");
        }
    }

    private void validarTraslapeHorario(String runDoctor, LocalDate fecha, LocalTime horaInicio) {
        LocalTime horaFin = horaInicio.plusMinutes(29);
        List<Evento> eventosDelDia = eventoRepository.findByRunDoctorOrderByFechaAscHoraAsc(runDoctor)
                .stream().filter(e -> e.getFecha().equals(fecha)).toList();

        for (Evento e : eventosDelDia) {
            LocalTime eInicio = e.getHora();
            LocalTime eFin = eInicio.plusMinutes(29);

            if (horaInicio.isBefore(eFin) && horaFin.isAfter(eInicio)) {
                throw new HorarioNoDisponibleException(
                    String.format("Conflicto: El horario choca con un(a) '%s' a las %s", 
                    e.getTipoEvento().getNombreTipo(), eInicio)
                );
            }
        }
    }

    private void validarPropiedad(ActividadPersonal actividad, String runDoctorToken) {
        if (!actividad.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso para gestionar esta actividad.");
        }
    }

    // ======================================
    // WEBCLIENTS
    // ======================================

    private DoctorResponseDTO obtenerDatosDoctor(String runDoctor) {
        try {
            return webClientBuilder.build().get()
                    .uri(urlDoctores + "/" + runDoctor)
                    .retrieve()
                    .bodyToMono(DoctorResponseDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException("Doctor no encontrado: " + runDoctor);
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al obtener datos del doctor", e);
        }
    }

    private void enviarANotificaciones(NotificacionRequestDTO dto) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(urlNotificaciones)
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                    "Error al conectar con MS-NOTIFICACIONES", e);
        }
    }

    private void eliminarNotificacionesPorEvento(Long idEvento) {
        try {
            // Nota: Este endpoint debe existir en MS-NOTIFICACIONES para borrar por evento
            webClientBuilder.build().delete()
                    .uri(urlNotificaciones + "/evento/" + idEvento)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return; // Si no hay notificaciones, no hay nada que eliminar
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al eliminar notificaciones de la actividad", e);
        }
    }

    // ======================================
    // MAPEOS DTO
    // ======================================

    private ActividadPersonalResponseDTO mapearAResponse(ActividadPersonal actividad) {
        ActividadPersonalResponseDTO res = new ActividadPersonalResponseDTO();
        res.setIdEvento(actividad.getIdEvento());
        res.setFecha(actividad.getFecha());
        res.setHora(actividad.getHora());
        res.setNombreActividad(actividad.getNombreActividad());
        res.setDescripcion(actividad.getDescripcion());
        return res;
    }
}