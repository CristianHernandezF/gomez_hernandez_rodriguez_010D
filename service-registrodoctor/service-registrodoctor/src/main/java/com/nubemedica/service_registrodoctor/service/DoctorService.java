package com.nubemedica.service_registrodoctor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registrodoctor.dto.*;
import com.nubemedica.service_registrodoctor.exceptions.*;
import com.nubemedica.service_registrodoctor.model.RegistroDoctor;
import com.nubemedica.service_registrodoctor.repository.RegistroDoctorRepository;

import jakarta.transaction.Transactional;

@Service
public class DoctorService {

    @Autowired
    private RegistroDoctorRepository doctorRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SECCIÓN DOCTOR
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Transactional
    public DoctorResponse registrarDoctor(RegistrarDoctorRequest request) {

        // 1. Validar duplicados (Email, RUN, Teléfono)
        if (doctorRepository.existsById(request.getRunDoctor())) {
            throw new DatoDuplicadoException("Ya existe un doctor con el RUN: " + request.getRunDoctor());
        }
        if (doctorRepository.existsByCorreo(request.getCorreo())) {
            throw new DatoDuplicadoException("Ya existe un doctor con el correo: " + request.getCorreo());
        }
        if (doctorRepository.existsByTelefono(request.getTelefono())) {
            throw new DatoDuplicadoException("Ya existe un doctor con el teléfono: " + request.getTelefono());
        }

        // 2. Crear dirección en MS-DIRECCIONES 
        // Obtenemos la respuesta completa (ID, calle, comuna, region)
        DireccionResponse direccionEnriquecida = crearDireccionEnMS(request.getDireccion());

        // 3. Crear instancia del modelo
        RegistroDoctor doctor = new RegistroDoctor();
        doctor.setRunDoctor(request.getRunDoctor());
        doctor.setCorreo(request.getCorreo());
        doctor.setPriNombre(request.getPriNombre());
        doctor.setSegNombre(request.getSegNombre());
        doctor.setApaPaterno(request.getApaPaterno());
        doctor.setApaMaterno(request.getApaMaterno());
        doctor.setTelefono(request.getTelefono());
        
        // Guardamos el ID de la dirección para la base de datos
        doctor.setIdDireccion(direccionEnriquecida.getIdDireccion());

        // 4. Crear credenciales en MS-LOGIN
        crearLoginUsuario(request);

        // 5. Guardar en la base de datos local
        RegistroDoctor guardado = doctorRepository.save(doctor);
        
        // Seteamos el DTO de dirección al campo @Transient del modelo guardado
        guardado.setDatosDireccion(direccionEnriquecida);

        // 7. Retornar el DTO de respuesta final
        return mapearADoctorResponse(guardado);
    }

    public List<DoctorResponse> listarTodos() {
        return doctorRepository.findAll().stream()
                .map(this::enriquecerConDireccion) // Llena el @Transient datosDireccion
                .map(this::mapearADoctorResponse)  // Convierte Entidad -> DTO
                .collect(Collectors.toList());
    }

    public DoctorResponse obtenerPorRun(String runDoctor) {
        RegistroDoctor doctor = doctorRepository.findByRunDoctor(runDoctor);
        if (doctor == null) {
            throw new NoExisteDoctorException(runDoctor);
        }
        return mapearADoctorResponse(enriquecerConDireccion(doctor));
    }

    @Transactional
    public void eliminarDoctor(String runDoctor) {
        RegistroDoctor doctor = doctorRepository.findByRunDoctor(runDoctor);
        if (doctor == null) {
            throw new NoExisteDoctorException(runDoctor);
        }

        eliminarDatosCalendarioEnMS(runDoctor);

        if (doctor.getIdDireccion() != null) {
            eliminarDireccionEnMS(doctor.getIdDireccion());
        }
        doctorRepository.deleteByRunDoctor(runDoctor);
    }

    @Transactional
    public DoctorResponse actualizarDoctor(String runDoctor, ActualizarDoctorRequest request) {
        RegistroDoctor existente = doctorRepository.findByRunDoctor(runDoctor);

        if (existente == null) {
            throw new NoExisteDoctorException(runDoctor);
        }

        // Validar correo/teléfono duplicado solo si cambiaron
        validarDuplicadosActualizacion(existente, request);

        // Actualizar datos básicos
        existente.setCorreo(request.getCorreo());
        existente.setPriNombre(request.getPriNombre());
        existente.setSegNombre(request.getSegNombre());
        existente.setApaPaterno(request.getApaPaterno());
        existente.setApaMaterno(request.getApaMaterno());
        existente.setTelefono(request.getTelefono());

        // Actualizar dirección en MS-DIRECCIONES
        actualizarDireccionEnMS(existente.getIdDireccion(), request.getDireccion());

        // Guardar, enriquecer y mapear a DTO
        RegistroDoctor guardado = doctorRepository.save(existente);
        return mapearADoctorResponse(enriquecerConDireccion(guardado));
    }

    // --- MÉTODOS DE APOYO Y COMUNICACIÓN ---

    private RegistroDoctor enriquecerConDireccion(RegistroDoctor doctor) {
        if (doctor.getIdDireccion() == null) return doctor;
        try {
            DireccionResponse dir = webClientBuilder.build().get()
                    .uri("http://localhost:8083/api/v1/direcciones/" + doctor.getIdDireccion())
                    .retrieve()
                    .bodyToMono(DireccionResponse.class) // TIPADO FUERTE
                    .block();
            doctor.setDatosDireccion(dir);
        } catch (Exception e) {
            doctor.setDatosDireccion(null);
        }
        return doctor;
    }

    private DireccionResponse crearDireccionEnMS(DireccionRequest direccionReq) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8083/api/v1/direcciones")
                    .bodyValue(direccionReq)
                    .retrieve()
                    .bodyToMono(DireccionResponse.class) // RECIBIMOS DTO PLANO
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al crear dirección en MS-DIRECCIONES", e);
        }
    }

    private void actualizarDireccionEnMS(Long idDireccion, DireccionRequest direccionReq) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8083/api/v1/direcciones/" + idDireccion)
                    .bodyValue(direccionReq)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al actualizar dirección en MS-DIRECCIONES", e);
        }
    }

    private void crearLoginUsuario(RegistrarDoctorRequest request) {
        LoginUsuarioRequest login = new LoginUsuarioRequest(
                request.getRunDoctor(),
                request.getCorreo(),
                request.getContrasena(),
                request.getTelefono()
        );
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8082/api/v1/auth/register")
                    .bodyValue(login)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al registrar credenciales en MS-LOGIN", e);
        }
    }

    private void eliminarDireccionEnMS(Long idDireccion) {
        try {
            webClientBuilder.build().delete()
                    .uri("http://localhost:8083/api/v1/direcciones/" + idDireccion)
                    .retrieve().bodyToMono(Void.class).block();
        } catch (Exception e) {
            System.err.println("Error eliminando dirección: " + e.getMessage());
        }
    }

    private void eliminarDatosCalendarioEnMS(String runDoctor) {
        try {
            webClientBuilder.build().delete()
                    .uri("http://localhost:8086/api/v1/calendario/agenda/doctor/" + runDoctor)
                    .retrieve().bodyToMono(Void.class).block();
        } catch (Exception e) {
            System.err.println("Error eliminando agenda: " + e.getMessage());
        }
    }

    private DoctorResponse mapearADoctorResponse(RegistroDoctor doctor) {
        DoctorResponse res = new DoctorResponse();
        res.setRunDoctor(doctor.getRunDoctor());
        res.setCorreo(doctor.getCorreo());
        res.setTelefono(doctor.getTelefono());
        
        // Construcción del nombre completo
        String nombreCompleto = doctor.getPriNombre() + 
                                (doctor.getSegNombre() != null ? " " + doctor.getSegNombre() : "") + 
                                " " + doctor.getApaPaterno() + 
                                " " + doctor.getApaMaterno();
        res.setNombreCompleto(nombreCompleto);

        // Pasamos la dirección que ya fue enriquecida por el método enriquecerConDireccion
        res.setDireccion(doctor.getDatosDireccion()); 
        
        return res;
    }

    private void validarDuplicadosActualizacion(RegistroDoctor existente, ActualizarDoctorRequest request) {
        if (!existente.getCorreo().equals(request.getCorreo()) && doctorRepository.existsByCorreo(request.getCorreo())) {
            throw new DatoDuplicadoException("El correo " + request.getCorreo() + " ya está registrado");
        }
        if (!existente.getTelefono().equals(request.getTelefono()) && doctorRepository.existsByTelefono(request.getTelefono())) {
            throw new DatoDuplicadoException("El teléfono " + request.getTelefono() + " ya está registrado");
        }
    }
}