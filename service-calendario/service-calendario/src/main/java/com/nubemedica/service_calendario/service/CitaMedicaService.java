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
public class CitaMedicaService {

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;
    
    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TipoEventoRepository tipoEventoRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${ms.estadocita.url:http://localhost:8087/api/v1/estadocita}")
    private String urlEstadoCita;

    @Value("${ms.doctores.url:http://localhost:8085/api/v1/doctores}")
    private String urlDoctores;

    @Value("${ms.pacientes.url:http://localhost:8084/api/v1/pacientes}")
    private String urlPacientes;

    @Value("${ms.telemedicina.url:http://localhost:8088/api/v1/telemedicina}")
    private String urlTelemedicina;

    @Value("${ms.notificaciones.url:http://localhost:8089/api/v1/notificaciones}")
    private String urlNotificaciones;

    // ======================================
    // MÉTODOS PÚBLICOS (LÓGICA DE NEGOCIO)
    // ======================================

    @Transactional
    public CitaMedicaResponseDTO crearCitaMedica(CitaMedicaRequestDTO request, String runDoctor) {
        validarFecha(request.getFecha());
        validarTraslapeHorario(runDoctor, request.getFecha(), request.getHora());
        validarRelacionDoctorPaciente(runDoctor, request.getRunPaciente());

        TelemedicinaResponseDTO tele = generarTelemedicina();

        CitaMedica cita = new CitaMedica();
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setRunDoctor(runDoctor);
        cita.setRunPaciente(request.getRunPaciente());
        cita.setMotivoConsulta(request.getMotivoConsulta());
        cita.setTipoEvento(tipoEventoRepository.findByNombreTipo("Cita Médica"));
        cita.setIdEstadoCitaMedica(crearEstadoInicialEnMS());
        cita.setIdSesionTelemedicina(tele.getIdSesionTelemedicina()); 
        
        CitaMedica guardada = citaMedicaRepository.save(cita);

        programarNotificacionesCita(guardada, tele.getLinkAcceso(), "PROGRAMADA");

        return crearRespuestaCita(guardada);
    }

    @Transactional
    public CitaMedicaResponseDTO actualizarCitaMedica(Long id, CitaMedicaRequestDTO request, String runDoctorToken) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        validarPropiedad(cita, runDoctorToken);

        if (!cita.getFecha().equals(request.getFecha()) || !cita.getHora().equals(request.getHora())) {
            validarFecha(request.getFecha());
            validarTraslapeHorario(runDoctorToken, request.getFecha(), request.getHora());
        }

        String linkActual = "";
        if (!cita.getFecha().equals(request.getFecha())) {
            TelemedicinaResponseDTO nuevoTele = regenerarTelemedicina(cita.getIdSesionTelemedicina());
            cita.setIdSesionTelemedicina(nuevoTele.getIdSesionTelemedicina());
            linkActual = nuevoTele.getLinkAcceso();
        } else {
            linkActual = obtenerLinkTelemedicina(cita.getIdSesionTelemedicina());
        }

        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setMotivoConsulta(request.getMotivoConsulta());
        cita.setRunPaciente(request.getRunPaciente());

        if (request.getEstadoCitaMedica() != null) {
            actualizarEstadoEnMS(cita.getIdEstadoCitaMedica(), request.getEstadoCitaMedica());
        }

        CitaMedica guardada = citaMedicaRepository.save(cita);
        programarNotificacionesCita(guardada, linkActual, "ACTUALIZADA");

        return crearRespuestaCita(guardada);
    }

    @Transactional
    public void eliminarCitaMedica(Long id, String runDoctorToken) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        validarPropiedad(cita, runDoctorToken);

        programarNotificacionesCita(cita, "N/A", "CANCELADA");
        eliminarTelemedicina(cita.getIdSesionTelemedicina());
        eliminarEstadoCitaEnMS(cita.getIdEstadoCitaMedica());
        
        citaMedicaRepository.deleteById(id);
    }

    public List<CitaMedicaResponseDTO> listarCitasMedicasPorDoctor(String runDoctorToken) {
        return citaMedicaRepository.findByRunDoctor(runDoctorToken).stream()
                .map(this::crearRespuestaCita)
                .toList();
    }

    public CitaMedicaResponseDTO obtenerCitaMedicaPorId(Long id, String runDoctorToken) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        validarPropiedad(cita, runDoctorToken);
        return crearRespuestaCita(cita);
    }

    @Transactional
    public void eliminarCitasEntreDoctorYPaciente(String runDoctor, String runPaciente) {
        List<CitaMedica> citas = citaMedicaRepository.findByRunDoctorAndRunPaciente(runDoctor, runPaciente);
        for (CitaMedica cita : citas) {
            eliminarEstadoCitaEnMS(cita.getIdEstadoCitaMedica());
            eliminarTelemedicina(cita.getIdSesionTelemedicina());
        }
        citaMedicaRepository.deleteByRunDoctorAndRunPaciente(runDoctor, runPaciente);
    }

    private void programarNotificacionesCita(CitaMedica cita, String link, String accion) {
        LocalDate fechaEnvio = cita.getFecha().minusDays(1);
        
        if (accion.equals("CANCELADA") || fechaEnvio.isBefore(LocalDate.now())) {
            fechaEnvio = LocalDate.now();
        }

        PacienteDTO pac = obtenerDatosPaciente(cita.getRunPaciente());
        DoctorResponseDTO doc = obtenerDatosDoctor(cita.getRunDoctor());

        NotificacionRequestDTO notifPac = new NotificacionRequestDTO();
        notifPac.setCorreoDestino(pac.getCorreo());
        notifPac.setAsunto("Cita Médica " + accion);
        notifPac.setMensaje("Hola " + pac.getNombreCompleto() + ", su cita con el Dr. " + doc.getNombreCompleto() + 
                 " ha sido " + accion + ". Fecha: " + cita.getFecha() + " Hora: " + cita.getHora() + ". Link: " + link);
        notifPac.setFechaEnvio(fechaEnvio);
        notifPac.setHoraEnvio(LocalTime.of(18, 0));
        notifPac.setIdEvento(cita.getIdEvento());
        enviarANotificaciones(notifPac);

        NotificacionRequestDTO notifDoc = new NotificacionRequestDTO();
        notifDoc.setCorreoDestino(doc.getCorreo());
        notifDoc.setAsunto("Agenda: Cita " + accion);
        notifDoc.setMensaje("Se ha registrado una cita " + accion + " con el paciente " + pac.getNombreCompleto() + 
                 " el día " + cita.getFecha() + " a las " + cita.getHora());
        notifDoc.setFechaEnvio(fechaEnvio);
        notifDoc.setHoraEnvio(LocalTime.of(18, 0));
        notifDoc.setIdEvento(cita.getIdEvento());
        enviarANotificaciones(notifDoc);
    }

    // ======================================
    // VALIDACIONES
    // ======================================

    private void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten fechas pasadas");
        }
    }

    private void validarTraslapeHorario(String runDoctor, LocalDate fecha, LocalTime horaInicio) {
        LocalTime horaFin = horaInicio.plusMinutes(29);
        List<Evento> eventosDelDia = eventoRepository.findByRunDoctorOrderByFechaAscHoraAsc(runDoctor)
                .stream().filter(e -> e.getFecha().equals(fecha)).toList();

        for (Evento e : eventosDelDia) {
            LocalTime existenteInicio = e.getHora();
            LocalTime existenteFin = existenteInicio.plusMinutes(29);
            if (horaInicio.isBefore(existenteFin) && horaFin.isAfter(existenteInicio)) {
                throw new HorarioNoDisponibleException(
                    String.format("Conflicto de horario: Ya existe un evento ('%s') programado a las %s", 
                    e.getTipoEvento().getNombreTipo(), existenteInicio)
                );
            }
        }
    }

    private void validarPropiedad(CitaMedica cita, String runDoctorToken) {
        if (!cita.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso sobre esta cita médica.");
        }
    }

    private void validarRelacionDoctorPaciente(String runDoctor, String runPaciente) {
        try {
            Boolean existe = webClientBuilder.build()
                                            .get()
                                            .uri(urlDoctores + "/atenciones/existe/" + runDoctor + "/" + runPaciente)
                                            .retrieve()
                                            .bodyToMono(Boolean.class)
                                            .block();
            if (Boolean.FALSE.equals(existe)) {
                throw new RecursoNoEncontradoException("No existe una relación registrada entre el doctor y el paciente.");
            }
        } catch (RecursoNoEncontradoException e) { throw e; }
        catch (Exception e) { 
            throw new ComunicacionMicroservicioException("Error validando relación en MS-DOCTORES", e); }
    }

    // ======================================
    // WEBCLIENTS
    // ======================================

    private PacienteDTO obtenerDatosPaciente(String runPaciente) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(urlPacientes + "/" + runPaciente + "/resumen")
                    .retrieve()
                    .bodyToMono(PacienteDTO.class)
                    .block();

        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException(
                    "Paciente no encontrado: " + runPaciente);

        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                    "Error al obtener datos del paciente", e);
        }
    }

    private DoctorResponseDTO obtenerDatosDoctor(String runDoctor) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(urlDoctores + "/" + runDoctor)
                    .retrieve()
                    .bodyToMono(DoctorResponseDTO.class)
                    .block();

        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException(
                    "Doctor no encontrado: " + runDoctor);

        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                    "Error al obtener datos del doctor", e);
        }
    }

    private TelemedicinaResponseDTO generarTelemedicina() {
        try {
            return webClientBuilder.build().post()
                    .uri(urlTelemedicina + "/generar")
                    .retrieve()
                    .bodyToMono(TelemedicinaResponseDTO.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("No se pudo generar la sesión en MS-TELEMEDICINA", e);
        }
    }

    private TelemedicinaResponseDTO regenerarTelemedicina(Long idSesion) {
        try {
            return webClientBuilder.build().put()
                    .uri(urlTelemedicina + "/" + idSesion)
                    .retrieve()
                    .bodyToMono(TelemedicinaResponseDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException("No se pudo regenerar: La sesión de telemedicina no existe.");
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al regenerar sesión de telemedicina", e);
        }
    }

    private String obtenerLinkTelemedicina(Long idSesion) {
        if (idSesion == null) return "No disponible";
        try {
            TelemedicinaResponseDTO res = webClientBuilder.build().get()
                    .uri(urlTelemedicina + "/" + idSesion)
                    .retrieve()
                    .bodyToMono(TelemedicinaResponseDTO.class)
                    .block();
            return (res != null) ? res.getLinkAcceso() : "No disponible";
        } catch (WebClientResponseException.NotFound e) {
            throw new RecursoNoEncontradoException("No se encontró el link: La sesión " + idSesion + " no existe.");
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al obtener link de telemedicina", e);
        }
    }

    private void eliminarTelemedicina(Long idSesion) {
        if (idSesion == null) return;
        try {
            webClientBuilder.build().delete()
                    .uri(urlTelemedicina + "/" + idSesion)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return; // Si no existe, ya está "eliminada"
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al eliminar sesión en MS-TELEMEDICINA", e);
        }
    }

    private Long crearEstadoInicialEnMS() {
        try {
            EstadoCitaMedicaDTO req = new EstadoCitaMedicaDTO("Agendada", "Cita creada desde Calendario");
            return webClientBuilder.build().post().uri(urlEstadoCita).bodyValue(req)
                    .retrieve().bodyToMono(Long.class).block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al crear estado inicial en MS-ESTADOCITA", e);
        }
    }

    private EstadoCitaMedicaDTO obtenerEstadoCitaDesdeMS(Long idEstado) {
        if (idEstado == null) return null;
        try {
            return webClientBuilder.build()
                                    .get().uri(urlEstadoCita + "/" + idEstado)
                                    .retrieve()
                                    .bodyToMono(EstadoCitaMedicaDTO.class).block();
        }
        catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error obteniendo estado de cita", e);
        }
    }

    private void actualizarEstadoEnMS(Long idEstado, EstadoCitaMedicaDTO dto) {
        try {
            webClientBuilder.build()
                            .put()
                            .uri(urlEstadoCita + "/" + idEstado)
                            .bodyValue(dto)
                            .retrieve().bodyToMono(Void.class).block();
        } catch (Exception e) { 
            throw new ComunicacionMicroservicioException("Error al actualizar estado en MS-ESTADOCITA", e); 
        }
    }

    private void eliminarEstadoCitaEnMS(Long idEstado) {
        if (idEstado == null) return;
        try {
            webClientBuilder.build()
                            .delete()
                            .uri(urlEstadoCita + "/" + idEstado)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
        } catch (WebClientResponseException.NotFound e) {
            return; // Si no existe, ya está "eliminado"
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al eliminar estado en MS-ESTADOCITA", e);
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
            throw new ComunicacionMicroservicioException( "Error enviando notificación", e);
        }
    }

    // ======================================
    // MAPEOS DTO
    // ======================================

    private CitaMedicaResponseDTO crearRespuestaCita(CitaMedica cita) {
        CitaMedicaResponseDTO res = new CitaMedicaResponseDTO();
        res.setIdEvento(cita.getIdEvento());
        res.setFecha(cita.getFecha());
        res.setHora(cita.getHora());
        res.setMotivoConsulta(cita.getMotivoConsulta());
        res.setRunDoctor(cita.getRunDoctor());
        res.setRunPaciente(cita.getRunPaciente());
        res.setEstadoCita(obtenerEstadoCitaDesdeMS(cita.getIdEstadoCitaMedica()));
        return res;
    }
}