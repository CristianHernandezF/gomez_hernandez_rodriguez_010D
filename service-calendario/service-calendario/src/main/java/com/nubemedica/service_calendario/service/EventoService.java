package com.nubemedica.service_calendario.service;

import com.nubemedica.service_calendario.dto.EstadoCitaMedicaDTO;
import com.nubemedica.service_calendario.dto.EventoDTO;
import com.nubemedica.service_calendario.exceptions.ComunicacionMicroservicioException;
import com.nubemedica.service_calendario.exceptions.HorarioNoDisponibleException;
import com.nubemedica.service_calendario.model.ActividadPersonal;
import com.nubemedica.service_calendario.model.CitaMedica;
import com.nubemedica.service_calendario.model.Evento;
import com.nubemedica.service_calendario.repository.CitaMedicaRepository;
import com.nubemedica.service_calendario.repository.EventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${ms.estadocita.url:http://localhost:8087/api/v1/estadocita}")
    private String urlEstadoCita;

    // --- AGENDA GENERAL ---

    // Obtiene todos los eventos (Citas y Actividades) de un doctor ordenados cronológicamente.

    public List<EventoDTO> obtenerAgendaDoctor(String runDoctor) {
        return eventoRepository.findByRunDoctorOrderByFechaAscHoraAsc(runDoctor)
                .stream()
                .map(this::mapearAEventoDTO)
                .toList();
    }

    // --- VALIDACIONES COMUNES ---

    // Valida si el horario está ocupado exactamente a la misma fecha y hora.
    // (Lógica original restaurada)
    public void validarHorarioDisponible(String runDoctor, LocalDate fecha, LocalTime hora) {
        boolean ocupado = eventoRepository.findByRunDoctorAndFechaAndHora(runDoctor, fecha, hora).isPresent();
        if (ocupado) {
            throw new HorarioNoDisponibleException("El horario ya está ocupado por otro evento.");
        }
    }

    // --- OPERACIONES MASIVAS ---

    // Elimina toda la agenda de un doctor, incluyendo la limpieza de estados en el MS externo.
    @Transactional
    public void eliminarTodaLaAgendaDelDoctor(String runDoctor) {
        // 1. Identificar citas médicas para borrar sus estados en el MS-ESTADOCITA
        List<CitaMedica> citas = citaMedicaRepository.findByRunDoctor(runDoctor);
        
        for (CitaMedica cita : citas) {
            if (cita.getIdEstadoCitaMedica() != null) {
                eliminarEstadoCitaEnMS(cita.getIdEstadoCitaMedica());
            }
        }

        // 2. Borrar todos los registros de la tabla base 'evento' (borra hijos en cascada)
        eventoRepository.deleteByRunDoctor(runDoctor);
    }

    // --- MÉTODOS PRIVADOS Y MAPEO ---

    private EventoDTO mapearAEventoDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setIdEvento(evento.getIdEvento());
        dto.setFecha(evento.getFecha());
        dto.setHora(evento.getHora());
        dto.setTipo(evento.getTipoEvento().getNombreTipo());
        dto.setColor(evento.getTipoEvento().getColorTipo());
        dto.setRunDoctor(evento.getRunDoctor());

        // Pattern Matching (Java 17) para llenar datos específicos según el tipo
        if (evento instanceof CitaMedica c) {
            dto.setRunPaciente(c.getRunPaciente());
            dto.setMotivoConsulta(c.getMotivoConsulta());
            dto.setEstadoCitaMedica(obtenerEstadoCitaDesdeMS(c.getIdEstadoCitaMedica()));
        } else if (evento instanceof ActividadPersonal a) {
            dto.setNombreActividad(a.getNombreActividad());
            dto.setDescripcion(a.getDescripcion());
        }
        
        return dto;
    }

    private EstadoCitaMedicaDTO obtenerEstadoCitaDesdeMS(Long idEstado) {
        if (idEstado == null) return null;
        try {
            return webClientBuilder.build().get()
                    .uri(urlEstadoCita + "/" + idEstado)
                    .retrieve()
                    .bodyToMono(EstadoCitaMedicaDTO.class)
                    .block();
        } catch (Exception e) {
            return new EstadoCitaMedicaDTO("Error", "No disponible");
        }
    }

    private void eliminarEstadoCitaEnMS(Long idEstado) {
        try {
            webClientBuilder.build().delete()
                    .uri(urlEstadoCita + "/" + idEstado)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            System.err.println("No se pudo borrar el estado " + idEstado + " en el MS externo.");
        }
    }
}