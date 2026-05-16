package com.nubemedica.service_registropacientes.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registropacientes.dto.*;
import com.nubemedica.service_registropacientes.exceptions.*;
import com.nubemedica.service_registropacientes.model.Paciente;
import com.nubemedica.service_registropacientes.repository.PacienteRepository;

import jakarta.transaction.Transactional;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    // MÉTODO: GUARDAR / ASOCIAR PACIENTE
    @Transactional
    public PacienteRegistroResponse guardarPaciente(PacienteRegistroRequest request, String runDoctor) {
        
        Optional<Paciente> pacienteExistente = pacienteRepository.findById(request.getRunPaciente());
        // 1. Si el paciente ya existe en el sistema global
        if (pacienteExistente.isPresent()) {
            Paciente existente = pacienteExistente.get();
            // Solo creamos la relación en MS-DOCTOR
            asociarDoctorAPaciente(runDoctor, existente.getRunPaciente());
            enriquecerConDireccion(existente);
            return new PacienteRegistroResponse(
                    existente,
                    "El paciente ya existe. Se ha creado la relación con usted exitosamente.",
                    true
            );
        }     

        // 2. Validar duplicados de contacto
        validarDatosUnicos(request.getCorreo(), request.getNumTelefono());

        // 3. Crear dirección en MS-DIRECCION
        Long idDireccion = crearDireccionEnMS(request.getDireccion());

        // 4. Crear paciente nuevo
        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setRunPaciente(request.getRunPaciente());
        nuevoPaciente.setCorreo(request.getCorreo());
        nuevoPaciente.setPriNombre(request.getPriNombre());
        nuevoPaciente.setSegNombre(request.getSegNombre());
        nuevoPaciente.setApaPaterno(request.getApaPaterno());
        nuevoPaciente.setApaMaterno(request.getApaMaterno());
        nuevoPaciente.setNumTelefono(request.getNumTelefono());
        nuevoPaciente.setIdDireccion(idDireccion);

        Paciente guardado = pacienteRepository.save(nuevoPaciente);

        // 5. Crear la relación en MS-DOCTOR
        asociarDoctorAPaciente(runDoctor, guardado.getRunPaciente());

        return new PacienteRegistroResponse(
                enriquecerConDireccion(guardado),
                "Paciente registrado y asociado exitosamente.",
                false);
    }

    // MÉTODOS DE LECTURA (PROTEGIDOS)


    public Paciente obtenerPacientePorRun(String runPaciente, String runDoctorToken) {
        // SEGURIDAD: Validar que el paciente pertenece al doctor
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);

        return pacienteRepository.findById(runPaciente)
                .map(this::enriquecerConDireccion)
                .orElseThrow(() -> new NoExistePacienteException(runPaciente));   
    }

    public List<Paciente> listarPacientesDeUnDoctor(String runDoctorToken) {
        // 1. Obtenemos los RUNs desde el microservicio de doctores
        List<String> runsAsociados = obtenerRunsAsociados(runDoctorToken);
        System.out.println("RUNs asociados obtenidos de MS-DOCTORES: " + runsAsociados);
        // 2. BUSCAMOS SOLO ESOS IDs (findAllById) y enriquecemos
        return pacienteRepository.findAllById(runsAsociados).stream()
            .map(this::enriquecerConDireccion)
            .toList();
    }

    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll().stream()
                .map(this::enriquecerConDireccion)
                .toList();
    }

    // ==========================================
    // MÉTODOS DE ACTUALIZACIÓN / ELIMINACIÓN
    // ==========================================

    @Transactional
    public Paciente actualizarPaciente(String runPaciente, ActualizarPacienteRequest request, String runDoctorToken) {
        // SEGURIDAD: Validar propiedad
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);

        Paciente existente = pacienteRepository.findById(runPaciente)
                .orElseThrow(() -> new NoExistePacienteException(runPaciente));

        // Validar correos/teléfonos duplicados con otros pacientes
        validarDatosUnicosParaActualizacion(existente, request);

        // Actualizar datos
        existente.setCorreo(request.getCorreo());
        existente.setPriNombre(request.getPriNombre());
        existente.setSegNombre(request.getSegNombre());
        existente.setApaPaterno(request.getApaPaterno());
        existente.setApaMaterno(request.getApaMaterno());
        existente.setNumTelefono(request.getNumTelefono());

        Paciente actualizado = pacienteRepository.save(existente);
        
        // Actualizar dirección en MS-DIRECCION
        actualizarDireccionEnMS(existente.getIdDireccion(), request.getDireccion());    

        return enriquecerConDireccion(actualizado);
    }

    //Seccion Eliminar

    @Transactional
    public void eliminarRelacionDoctorPaciente(String runPaciente, String runDoctorToken) {

        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);
        RelacionDoctorPacienteDTO dto = new RelacionDoctorPacienteDTO(runDoctorToken, runPaciente);
        desasociarPacienteDeDoctor(dto);
    }

    //Metodos de apoyo y validacion

    private void validarDatosUnicos(String correo, String telefono) {
        if (pacienteRepository.existsByCorreo(correo)) 
            throw new DatoDuplicadoException("El correo " + correo + " ya existe.");
        if (pacienteRepository.existsByNumTelefono(telefono)) 
            throw new DatoDuplicadoException("El teléfono " + telefono + " ya existe.");
    }

    private void validarDatosUnicosParaActualizacion(Paciente existente, ActualizarPacienteRequest request) {
        if (!existente.getCorreo().equals(request.getCorreo()) && pacienteRepository.existsByCorreo(request.getCorreo()))
            throw new DatoDuplicadoException("El nuevo correo ya está en uso.");
        if (!existente.getNumTelefono().equals(request.getNumTelefono()) && pacienteRepository.existsByNumTelefono(request.getNumTelefono()))
            throw new DatoDuplicadoException("El nuevo teléfono ya está en uso.");
    }

    public PacienteResumenDTO obtenerResumenPaciente(String runPaciente) {
        return pacienteRepository.findResumenByRun(runPaciente)
                .orElseThrow(() -> new NoExistePacienteException(runPaciente));
    }

    // ==========================================
    // COMUNICACIÓN EXTERNA (WEBCLIENT)
    // ==========================================
    
    private void validarRelacionDoctorPaciente(String runDoctor, String runPaciente) {
        try {
            Boolean existe = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8085/api/v1/atenciones/existe/" + runDoctor + "/" + runPaciente)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if (Boolean.FALSE.equals(existe)) {
                throw new AccesoDenegadoException("Acceso denegado: Este paciente no está en su lista de atención.");
            }
        } catch (AccesoDenegadoException e) { throw e; }
        catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error de validación de seguridad en MS-DOCTORES", e);
        }
    }

    private Paciente enriquecerConDireccion(Paciente paciente) {
        if (paciente.getIdDireccion() == null) return paciente;
        try {
            Object dir = webClientBuilder.build().get()
                    .uri("http://localhost:8083/api/v1/direcciones/" + paciente.getIdDireccion())
                    .retrieve()
                    .bodyToMono(Object.class).block();
            paciente.setDatosDireccion(dir);
        } catch (Exception e) {
            paciente.setDatosDireccion(null);
            throw new ComunicacionMicroservicioException("Información de dirección no disponible", e);
        }
        return paciente;
    }

    private void asociarDoctorAPaciente(String runDoctor, String runPaciente) {
        try {
            RelacionDoctorPacienteDTO dto = new RelacionDoctorPacienteDTO(runDoctor, runPaciente);
            webClientBuilder.build().post()
                    .uri("http://localhost:8085/api/v1/atenciones")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("No se pudo asociar el paciente al doctor en MS-DOCTORES", e);
        }
    }

    private Long crearDireccionEnMS(DireccionDTO dto) {
        try {
            return webClientBuilder.build().post()
                    .uri("http://localhost:8083/api/v1/direcciones")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al crear dirección en MS-DIRECCION", e);
        }
    }

    private void actualizarDireccionEnMS(Long id, DireccionDTO direccion) {
        try {
            webClientBuilder.build().put()
                    .uri("http://localhost:8083/api/v1/direcciones/" + id)
                    .bodyValue(direccion)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al actualizar dirección en MS-DIRECCION", e);
        }
    }

    private List<String> obtenerRunsAsociados(String runDoctorToken) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8085/api/v1/atenciones/doctor/" + runDoctorToken)
                    .retrieve()
                    .bodyToMono(String[].class)  
                    .map(Arrays::asList)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Fallo al conectar con MS-DOCTORES para obtener lista de RUNs", e);
        }
    }

    private void desasociarPacienteDeDoctor(RelacionDoctorPacienteDTO dto) {
        try {
            // Realizamos la conexión física enviando el DTO como cuerpo
            webClientBuilder.build()
                    .method(HttpMethod.DELETE)
                    .uri("http://localhost:8085/api/v1/atenciones/" + dto.getRunPaciente() + "/desasociar")
                    .bodyValue(dto.getRunDoctor()) // <--- USANDO EL DTO OFICIAL
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Fallo de comunicación: No se pudo desasociar el paciente en MS-DOCTORES", e);
        }
    }

}