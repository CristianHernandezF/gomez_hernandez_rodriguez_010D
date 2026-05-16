package com.nubemedica.service_calendario.service;

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.exceptions.*;
import com.nubemedica.service_calendario.model.*;
import com.nubemedica.service_calendario.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CalendarioService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private CitaMedicaRepository citaMedicaRepository;
    @Autowired
    private ActividadPersonalRepository actividadPersonalRepository;
    @Autowired
    private TipoEventoRepository tipoEventoRepository;
    @Autowired
    private WebClient.Builder webClientBuilder;

    // CRUD CITA MÉDICA

    @Transactional
    public CitaMedicaResponseDTO crearCitaMedica(CitaMedicaRequestDTO request, String runDoctor) {
        validarFecha(request.getFecha());
        validarHorarioDisponible(runDoctor, request.getFecha(), request.getHora());

        // Validar si el doctor tiene permiso de atender a este paciente (MS-DOCTORES)
        validarRelacionDoctorPaciente(runDoctor, request.getRunPaciente());

        Long idEstado = crearEstadoInicial(); // MS-ESTADOCITA (Retorna ID)

        System.out.println("ID generado por MS-ESTADOCITA: " + idEstado); // DEBUG: Revisa que no sea null aquí

        TipoEvento tipo = buscarTipoEvento("Cita Médica");


        CitaMedica cita = new CitaMedica();
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setRunDoctor(runDoctor); // RUN verificado por Gateway
        cita.setRunPaciente(request.getRunPaciente());
        cita.setMotivoConsulta(request.getMotivoConsulta());
        cita.setTipoEvento(tipo);
        cita.setIdEstadoCitaMedica(idEstado);

        return crearRespuestaCita(citaMedicaRepository.save(cita), idEstado);
    }

    public CitaMedicaResponseDTO obtenerCitaMedicaPorId(Long id, String runDoctorToken) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        if (!cita.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso para ver esta cita.");
        }

        return crearRespuestaCita(cita, cita.getIdEstadoCitaMedica());
    }

    public List<CitaMedicaResponseDTO> listarCitasPorDoctor(String runDoctorToken) {
        return citaMedicaRepository.findByRunDoctor(runDoctorToken).stream()
                .map(cita -> crearRespuestaCita(cita, cita.getIdEstadoCitaMedica()))
                .toList();
    }

    @Transactional
    public CitaMedicaResponseDTO actualizarCitaMedica(Long id, CitaMedicaRequestDTO request, String runDoctorToken) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        // es el dueño?
        if (!cita.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No puedes modificar citas de otros doctores.");
        }

        // Validar cambio de horario si aplica
        if (!cita.getFecha().equals(request.getFecha()) || !cita.getHora().equals(request.getHora())) {
            validarFecha(request.getFecha());
            validarHorarioDisponible(runDoctorToken, request.getFecha(), request.getHora());
        }

        // Actualizar datos de la cita
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setMotivoConsulta(request.getMotivoConsulta());
        cita.setRunPaciente(request.getRunPaciente());

        // ACTUALIZAR ESTADO 
        Long idEstadoCita = cita.getIdEstadoCitaMedica();
        EstadoCitaMedicaDTO dto = request.getEstadoCitaMedica();
        actualizarEstadoPorNombreEnMS(idEstadoCita, dto);

        CitaMedica actualizada = citaMedicaRepository.save(cita);
        return crearRespuestaCita(actualizada, actualizada.getIdEstadoCitaMedica());
    }

    @Transactional
    public void eliminarCitaMedica(Long id, String runDoctorToken) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no existe"));

        // SEGURIDAD: Solo el dueño puede borrar
        if (!cita.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso para eliminar esta cita.");
        }

        citaMedicaRepository.deleteById(id);
    }

    // CRUD ACTIVIDAD PERSONAL

    @Transactional
    public ActividadPersonalResponseDTO crearActividadPersonal(ActividadPersonalRequestDTO request, String runDoctor) {
        validarFecha(request.getFecha());
        validarHorarioDisponible(runDoctor, request.getFecha(), request.getHora());

        TipoEvento tipo = buscarTipoEvento("Actividad Personal");

        ActividadPersonal actividad = new ActividadPersonal();
        actividad.setFecha(request.getFecha());
        actividad.setHora(request.getHora());
        actividad.setRunDoctor(runDoctor); // RUN verificado por Gateway
        actividad.setTipoEvento(tipo);
        actividad.setNombreActividad(request.getNombreActividad());
        actividad.setDescripcion(request.getDescripcion());

        return crearRespuestaActividad(actividadPersonalRepository.save(actividad));
    }

    public ActividadPersonalResponseDTO obtenerActividadPersonalPorId(Long id, String runDoctorToken) {
        ActividadPersonal act = actividadPersonalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));

        // VALIDACIÓN DE SEGURIDAD
        if (!act.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso para ver esta actividad");
        }

        return crearRespuestaActividad(act);
    }

    public List<ActividadPersonalResponseDTO> listarActividadesPorDoctor(String runDoctorToken) {
        // Al usar el RUN del token, garantizamos que solo vea las suyas
        return actividadPersonalRepository.findByRunDoctor(runDoctorToken).stream()
                .map(this::crearRespuestaActividad)
                .toList();
    }

    @Transactional
    public ActividadPersonalResponseDTO actualizarActividadPersonal(Long id, ActividadPersonalRequestDTO request, String runDoctorToken) {
        ActividadPersonal act = actividadPersonalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));

        // VALIDACIÓN DE SEGURIDAD: ¿Es el dueño?
        if (!act.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso para modificar esta actividad personal.");
        }

        // Si cambia fecha u hora, validar disponibilidad
        if (!act.getFecha().equals(request.getFecha()) || !act.getHora().equals(request.getHora())) {
            validarFecha(request.getFecha());
            validarHorarioDisponible(runDoctorToken, request.getFecha(), request.getHora());
        }

        act.setNombreActividad(request.getNombreActividad());
        act.setDescripcion(request.getDescripcion());
        act.setFecha(request.getFecha());
        act.setHora(request.getHora());

        return crearRespuestaActividad(actividadPersonalRepository.save(act));
    }

    @Transactional
    public void eliminarCitasEntreDoctorYPaciente(String runDoctor, String runPaciente) {
        // 1. Obtener las citas específicas para identificar los IDs de estado
        List<CitaMedica> citas = citaMedicaRepository.findByRunDoctorAndRunPaciente(runDoctor, runPaciente);

        // 2. Borrar los estados en MS-ESTADOCITA
        for (CitaMedica cita : citas) {
            if (cita.getIdEstadoCitaMedica() != null) {
                eliminarEstadoCitaEnMS(cita.getIdEstadoCitaMedica()); 
            }
        }

        // 3. Borrar las citas de la base de datos
        citaMedicaRepository.deleteByRunDoctorAndRunPaciente(runDoctor, runPaciente);
    }

    @Transactional
    public void eliminarActividadPersonal(Long id, String runDoctorToken) {
        ActividadPersonal act = actividadPersonalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no existe"));

        // VALIDACIÓN DE SEGURIDAD
        if (!act.getRunDoctor().equals(runDoctorToken)) {
            throw new AccesoDenegadoException("No tienes permiso para eliminar esta actividad");
        }

        actividadPersonalRepository.deleteById(id);
    }

    // ==========================================
    // AGENDA COMPLETA (Mezcla de tipos)
    // ==========================================

    public List<EventoDTO> obtenerAgendaDoctor(String runDoctor) {
        // Aquí usamos eventoRepository porque queremos todo (Citas + Actividades)
        return eventoRepository.findByRunDoctorOrderByFechaAscHoraAsc(runDoctor)
                .stream()
                .map(this::mapearAEventoDTO)
                .toList();
    }

    // ==========================================
    // MÉTODOS PRIVADOS Y MAPEOS
    // ==========================================
    @Transactional
    public void eliminarTodaLaAgendaDelDoctor(String runDoctor) {
        // 1. Buscar todas las citas médicas de este doctor para obtener los IDs de sus estados
        List<CitaMedica> citas = citaMedicaRepository.findByRunDoctor(runDoctor);
        
        // 2. Por cada cita, llamar al MS-ESTADOCITA para borrar el estado
        for (CitaMedica cita : citas) {
            if (cita.getIdEstadoCitaMedica() != null) {
                eliminarEstadoCitaEnMS(cita.getIdEstadoCitaMedica());
            }
        }

        // 3. Ahora sí, borrar todos los eventos (Citas y Actividades) de la base de datos
        eventoRepository.deleteByRunDoctor(runDoctor);
    }


    private void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten fechas pasadas");
        }
    }

    private void validarHorarioDisponible(String runDoctor, LocalDate fecha, LocalTime hora) {
        // Es vital usar eventoRepository aquí para que una Actividad Personal bloquee una Cita y viceversa
        boolean ocupado = eventoRepository.findByRunDoctorAndFechaAndHora(runDoctor, fecha, hora).isPresent();
        if (ocupado) throw new HorarioNoDisponibleException("El horario ya está ocupado");
    }

    private TipoEvento buscarTipoEvento(String nombre) {
        TipoEvento tipo = tipoEventoRepository.findByNombreTipo(nombre);
        if (tipo == null) throw new TipoEventoNoEncontradoException("Tipo " + nombre + " no configurado");
        return tipo;
    }

    private CitaMedicaResponseDTO crearRespuestaCita(CitaMedica cita, Long idEstado) {
        CitaMedicaResponseDTO res = new CitaMedicaResponseDTO();
        res.setIdEvento(cita.getIdEvento());
        res.setFecha(cita.getFecha());
        res.setHora(cita.getHora());
        res.setMotivoConsulta(cita.getMotivoConsulta());
        res.setRunDoctor(cita.getRunDoctor());
        res.setRunPaciente(cita.getRunPaciente());
        
        // Obtenemos el objeto DTO completo del microservicio
        EstadoCitaMedicaDTO estado = obtenerEstadoCita(idEstado);
        
        // CORRECCIÓN: Pasamos el objeto 'estado' completo, no solo el String
        if (estado != null) {
            res.setEstadoCita(estado); 
        }
        
        return res;
    }

    private ActividadPersonalResponseDTO crearRespuestaActividad(ActividadPersonal act) {
        ActividadPersonalResponseDTO res = new ActividadPersonalResponseDTO();
        res.setIdEvento(act.getIdEvento());
        res.setFecha(act.getFecha());
        res.setHora(act.getHora());
        res.setNombreActividad(act.getNombreActividad());
        res.setDescripcion(act.getDescripcion());
        return res;
    }

    private EventoDTO mapearAEventoDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setIdEvento(evento.getIdEvento());
        dto.setFecha(evento.getFecha());
        dto.setHora(evento.getHora());
        dto.setTipo(evento.getTipoEvento().getNombreTipo());
        dto.setColor(evento.getTipoEvento().getColorTipo());
        dto.setRunDoctor(evento.getRunDoctor());

        // En este método específico de agenda general, sí usamos pattern matching (Java 17+)
        // Es la forma más limpia de manejar una lista polimórfica
        if (evento instanceof CitaMedica c) {
            dto.setRunPaciente(c.getRunPaciente());
            dto.setMotivoConsulta(c.getMotivoConsulta());
            dto.setEstadoCitaMedica(obtenerEstadoCita(c.getIdEstadoCitaMedica()));
        } else if (evento instanceof ActividadPersonal a) {
            dto.setNombreActividad(a.getNombreActividad());
            dto.setDescripcion(a.getDescripcion());
        }
        return dto;
    }

    // ==========================================
    // COMUNICACIÓN WEBCLIENT
    // ==========================================

    private EstadoCitaMedicaDTO obtenerEstadoCita(Long idEstado) {
        try {
            return webClientBuilder.build().get()
                    .uri("http://localhost:8087/api/v1/estadocita/" + idEstado)
                    .retrieve()
                    .bodyToMono(EstadoCitaMedicaDTO.class)
                    .block();
        } catch (Exception e) { return null; }
    }

    private Long crearEstadoInicial() {
        try {
            EstadoCitaMedicaDTO req = new EstadoCitaMedicaDTO("Agendada", "Inicial");
            Long res= webClientBuilder.build().post()
                    .uri("http://localhost:8087/api/v1/estadocita")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();
            return res;
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al crear estado", e);
        }
    }

    private void validarRelacionDoctorPaciente(String runDoctor, String runPaciente) {
        try {
            Boolean ok = webClientBuilder.build().get()
                    .uri("http://localhost:8085/api/v1/atenciones/existe/" + runDoctor + "/" + runPaciente)
                    .retrieve().bodyToMono(Boolean.class).block();
            if (Boolean.FALSE.equals(ok)) throw new RecursoNoEncontradoException("No existe relación Dr-Paciente");
        } catch (RecursoNoEncontradoException e) { throw e; }
        catch (Exception e) { throw new ComunicacionMicroservicioException("Error validando relación", e); }
    }


    private void actualizarEstadoPorNombreEnMS(Long idEstadoCita, EstadoCitaMedicaDTO dto) {
        if (idEstadoCita == null) {
            throw new ComunicacionMicroservicioException("Error: La cita no tiene un ID de estado asociado.", null);
        }        
        try {
            webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8087/api/v1/estadocita/" + idEstadoCita)
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Esto te ayudará a ver el error REAL en la consola
            e.printStackTrace(); 
            throw new ComunicacionMicroservicioException("No se pudo actualizar el estado de la cita. Causa: " + e.getMessage(), e);
        }
    }

        // Nuevo método privado para la comunicación con MS-ESTADOCITA
    private void eliminarEstadoCitaEnMS(Long idEstado) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri("http://localhost:8087/api/v1/estadocita/" + idEstado)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Logueamos pero no detenemos el proceso si un estado no se pudo borrar
            System.err.println("No se pudo borrar el estado " + idEstado + ": " + e.getMessage());
        }
    }
}