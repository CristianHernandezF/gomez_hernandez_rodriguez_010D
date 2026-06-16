package com.nubemedica.service_registropacientes.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ms.fichamedica.url:http://localhost:8088/api/v1/fichas}")
    private String urlMsFichamedica;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MÉTODOS PÚBLICOS (NORMALIZADOS)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Transactional
    public PacienteRegistroResponse guardarPaciente(PacienteRegistroRequest request, String runDoctor) {
        Optional<Paciente> pacienteExistente = pacienteRepository.findById(request.getRunPaciente());

        // Caso: paciente ya existe
        if (pacienteExistente.isPresent()) {
            Paciente existente = pacienteExistente.get();

            asociarYCrearFicha(runDoctor, existente.getRunPaciente()); // ← una sola línea

            Paciente enriquecido = enriquecerConDireccion(existente);
            return new PacienteRegistroResponse(
                    mapearAPacienteResponse(enriquecido),
                    "El paciente ya existe. Se ha creado la relación con usted exitosamente.",
                    true
            );
        }

        // Caso: paciente nuevo
        validarDatosUnicos(request.getCorreo(), request.getNumTelefono());

        DireccionResponse direccionGuardada = crearDireccionEnMS(request.getDireccion());

        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setRunPaciente(request.getRunPaciente());
        nuevoPaciente.setCorreo(request.getCorreo());
        nuevoPaciente.setPriNombre(request.getPriNombre());
        nuevoPaciente.setSegNombre(request.getSegNombre());
        nuevoPaciente.setApaPaterno(request.getApaPaterno());
        nuevoPaciente.setApaMaterno(request.getApaMaterno());
        nuevoPaciente.setNumTelefono(request.getNumTelefono());
        nuevoPaciente.setIdDireccion(direccionGuardada.getIdDireccion());

        Paciente guardado = pacienteRepository.save(nuevoPaciente);

        asociarYCrearFicha(runDoctor, guardado.getRunPaciente()); // ← una sola línea

        guardado.setDatosDireccion(direccionGuardada);
        return new PacienteRegistroResponse(
                mapearAPacienteResponse(guardado),
                "Paciente registrado y asociado exitosamente.",
                false
        );
    }

    public PacienteResponse obtenerPacientePorRun(String runPaciente, String runDoctorToken) {
        // SEGURIDAD: Validar que el paciente pertenece al doctor
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);

        Paciente paciente = pacienteRepository.findById(runPaciente)
                .orElseThrow(() -> new NoExistePacienteException(runPaciente));
        
        return mapearAPacienteResponse(enriquecerConDireccion(paciente));
    }

    public List<PacienteResponse> listarPacientesDeUnDoctor(String runDoctorToken) {
        // 1. Obtenemos los RUNs desde el microservicio de doctores
        List<String> runsAsociados = obtenerRunsAsociados(runDoctorToken);
        
        // 2. Buscamos las entidades, las enriquecemos y mapeamos a Response
        return pacienteRepository.findAllById(runsAsociados).stream()
            .map(this::enriquecerConDireccion)
            .map(this::mapearAPacienteResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PacienteResponse actualizarPaciente(String runPaciente, ActualizarPacienteRequest request, String runDoctorToken) {
        // SEGURIDAD: Validar propiedad
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);

        Paciente existente = pacienteRepository.findById(runPaciente)
                .orElseThrow(() -> new NoExistePacienteException(runPaciente));

        // Validar correos/teléfonos duplicados
        validarDatosUnicosParaActualizacion(existente, request);

        // Actualizar datos
        existente.setCorreo(request.getCorreo());
        existente.setPriNombre(request.getPriNombre());
        existente.setSegNombre(request.getSegNombre());
        existente.setApaPaterno(request.getApaPaterno());
        existente.setApaMaterno(request.getApaMaterno());
        existente.setNumTelefono(request.getNumTelefono());

        // Actualizar dirección en MS-DIRECCION
        actualizarDireccionEnMS(existente.getIdDireccion(), request.getDireccion());

        Paciente actualizado = pacienteRepository.save(existente);
        
        return mapearAPacienteResponse(enriquecerConDireccion(actualizado));
    }

    @Transactional
    public void eliminarRelacionDoctorPaciente(String runPaciente, String runDoctorToken) {
        validarRelacionDoctorPaciente(runDoctorToken, runPaciente);
        RelacionDoctorPacienteDTO dto = new RelacionDoctorPacienteDTO(runDoctorToken, runPaciente);
        desasociarPacienteDeDoctor(dto);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MÉTODOS DE APOYO Y MAPEO
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Centraliza la creación del DTO de respuesta único
     */
    private PacienteResponse mapearAPacienteResponse(Paciente p) {
        return PacienteResponse.builder()
                .runPaciente(p.getRunPaciente())
                .correo(p.getCorreo())
                .numTelefono(p.getNumTelefono())
                .nombreCompleto(p.getPriNombre() + 
                                (p.getSegNombre() != null ? " " + p.getSegNombre() : "") + 
                                " " + p.getApaPaterno() + 
                                " " + p.getApaMaterno())
                .direccion(p.getDatosDireccion())
                .build();
    }

    private void asociarYCrearFicha(String runDoctor, String runPaciente) {
        asociarDoctorAPaciente(runDoctor, runPaciente);
            try {
                crearFichaEnMS(runPaciente, runDoctor);
            } catch (Exception e) {
                try {
                    desasociarPacienteDeDoctor(new RelacionDoctorPacienteDTO(runDoctor, runPaciente));
                } catch (Exception compensacionEx) {
                    // Compensación fallida — inconsistencia entre MS-DOCTORES y MS-FICHAMEDICA
                    // Requiere intervención manual o sistema de reconciliación
                }
                throw new ComunicacionMicroservicioException(
                    "Error al crear ficha médica. Se revirtió la asociación del doctor.", e
                );
        }
    }

    private Paciente enriquecerConDireccion(Paciente paciente) {
        if (paciente.getIdDireccion() == null) return paciente;
        try {
            DireccionResponse dir = webClientBuilder.build().get()
                    .uri("http://localhost:8083/api/v1/direcciones/" + paciente.getIdDireccion())
                    .retrieve()
                    .bodyToMono(DireccionResponse.class) // TIPADO FUERTE: No más Object
                    .block();
            paciente.setDatosDireccion(dir);
        } catch (Exception e) {
            paciente.setDatosDireccion(null);
        }
        return paciente;
    }

    private DireccionResponse crearDireccionEnMS(DireccionRequest dto) {
        try {
            return webClientBuilder.build().post()
                    .uri("http://localhost:8083/api/v1/direcciones")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(DireccionResponse.class) // Recibe el DTO plano completo
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al crear dirección en MS-DIRECCION", e);
        }
    }

    private void actualizarDireccionEnMS(Long id, DireccionRequest direccion) {
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

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VALIDACIONES Y COMUNICACIÓN DOCTOR
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

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
            throw new ComunicacionMicroservicioException("Error de validación en MS-DOCTORES", e);
        }
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
            throw new ComunicacionMicroservicioException("No se pudo asociar el paciente al doctor", e);
        }
    }

    private List<String> obtenerRunsAsociados(String runDoctorToken) {
        try {
            String[] runs = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8085/api/v1/atenciones/doctor/" + runDoctorToken)
                    .retrieve()
                    .bodyToMono(String[].class)
                    .block();
            return Arrays.asList(runs);
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Fallo al obtener RUNs de MS-DOCTORES", e);
        }
    }

    private void desasociarPacienteDeDoctor(RelacionDoctorPacienteDTO dto) {
        try {
            webClientBuilder.build()
                    .method(HttpMethod.DELETE)
                    .uri("http://localhost:8085/api/v1/atenciones/" + dto.getRunPaciente() + "/desasociar")
                    .bodyValue(dto.getRunDoctor())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("No se pudo desasociar el paciente", e);
        }
    }

    private void crearFichaEnMS(String runPaciente, String runDoctor) {
        try {
            webClientBuilder.build()
                .post()
                .uri(urlMsFichamedica + "/interno?runPaciente={rp}&runDoctor={rd}",
                    runPaciente, runDoctor)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException(
                "Error al crear la ficha médica en MS-FICHAMEDICA: " + e.getMessage(), e
            );
        }
    }

}