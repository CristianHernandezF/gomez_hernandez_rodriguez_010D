package com.nubemedica.service_registrodoctor.service;

import java.util.List;

import com.nubemedica.service_registrodoctor.dto.LoginUsuarioRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registrodoctor.dto.ActualizarDoctorRequest;
import com.nubemedica.service_registrodoctor.dto.DireccionDTO;
import com.nubemedica.service_registrodoctor.dto.RegistrarDoctorRequest;
import com.nubemedica.service_registrodoctor.exceptions.ComunicacionMicroservicioException;
import com.nubemedica.service_registrodoctor.exceptions.DatoDuplicadoException;
import com.nubemedica.service_registrodoctor.exceptions.NoExisteDoctorException;
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
    public RegistroDoctor registrarDoctor(RegistrarDoctorRequest request) {

        // 1. Validar duplicados
        if (doctorRepository.existsById(request.getRunDoctor())) {
            throw new DatoDuplicadoException("Ya existe un doctor con el RUN: " + request.getRunDoctor());
        }
        if (doctorRepository.existsByCorreo(request.getCorreo())) {
            throw new DatoDuplicadoException("Ya existe un doctor con el correo: " + request.getCorreo());
        }
        if (doctorRepository.existsByTelefono(request.getTelefono())) {
            throw new DatoDuplicadoException("Ya existe un doctor con el teléfono: " + request.getTelefono());
        }

        // 2. Crear dirección en MS de direcciones
        DireccionDTO direccionDTO = new DireccionDTO(request.getDireccion().getNombre(), request.getDireccion().getComunaId());
        Long idDireccion = crearDireccion(direccionDTO);

        // 3. Crear doctor
        RegistroDoctor doctor = new RegistroDoctor();
        doctor.setRunDoctor(request.getRunDoctor());
        doctor.setCorreo(request.getCorreo());
        doctor.setPriNombre(request.getPriNombre());
        doctor.setSegNombre(request.getSegNombre());
        doctor.setApaPaterno(request.getApaPaterno());
        doctor.setApaMaterno(request.getApaMaterno());
        doctor.setTelefono(request.getTelefono());
        doctor.setIdDireccion(idDireccion);

        crearLoginUsuario(request);

        // 4. Guardar y enriquecer
        RegistroDoctor guardado = doctorRepository.save(doctor);
        return enriquecerConDireccion(guardado);
    }

    public List<RegistroDoctor> listarTodos() {
        List<RegistroDoctor> lista = doctorRepository.findAll();
        lista.forEach(this::enriquecerConDireccion);
        return lista;
    }

    public RegistroDoctor obtenerPorRun(String runDoctor) {
        RegistroDoctor doctor = doctorRepository.findByRunDoctor(runDoctor);
        if (doctor != null) {
            return enriquecerConDireccion(doctor);
        }
        throw new NoExisteDoctorException(runDoctor);
    }

    @Transactional
    public void eliminarDoctor(String runDoctor) {
        RegistroDoctor doctor = doctorRepository.findByRunDoctor(runDoctor);
        if (doctor == null) {
            throw new NoExisteDoctorException(runDoctor);
        }

        // 1. AVISAR A MS-CALENDARIO (Este ya limpiará los estados de las citas por dentro)
        eliminarDatosCalendarioEnMS(runDoctor);

        // 2. BORRAR DIRECCIÓN EN MS-DIRECCION
        if (doctor.getIdDireccion() != null) {
            eliminarDireccionEnMS(doctor.getIdDireccion());
        }

        // 3. Borrar de su propia base de datos (Doctor y tabla intermedia Atenciones)
        doctorRepository.deleteByRunDoctor(runDoctor);
    }


    @Transactional
    public RegistroDoctor actualizarDoctor(String runDoctor, ActualizarDoctorRequest request) {
        RegistroDoctor existente = doctorRepository.findByRunDoctor(runDoctor);

        if (existente == null) {
            throw new NoExisteDoctorException(runDoctor);
        }

        // Validar correo duplicado solo si cambió
        if (!existente.getCorreo().equals(request.getCorreo()) &&
                doctorRepository.existsByCorreo(request.getCorreo())) {
            throw new DatoDuplicadoException("El correo " + request.getCorreo() + " ya está registrado");
        }

        // Validar teléfono duplicado solo si cambió
        if (!existente.getTelefono().equals(request.getTelefono()) &&
                doctorRepository.existsByTelefono(request.getTelefono())) {
            throw new DatoDuplicadoException("El teléfono " + request.getTelefono() + " ya está registrado");
        }

        // Actualizar datos
        existente.setCorreo(request.getCorreo());
        existente.setPriNombre(request.getPriNombre());
        existente.setSegNombre(request.getSegNombre());
        existente.setApaPaterno(request.getApaPaterno());
        existente.setApaMaterno(request.getApaMaterno());
        existente.setTelefono(request.getTelefono());

        // Actualizar dirección en MS de direcciones
        DireccionDTO direccionDTO = new DireccionDTO(request.getDireccion().getNombre(), request.getDireccion().getComunaId());
        actualizarDireccion(existente.getIdDireccion(), direccionDTO);

        RegistroDoctor guardado = doctorRepository.save(existente);
        return enriquecerConDireccion(guardado);
    }

// --- MÉTODOS DE COMUNICACIÓN ACTUALIZADOS ---

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
            // Cambio de RuntimeException a ComunicacionMicroservicioException
            throw new ComunicacionMicroservicioException("Error al comunicarse con MS-LOGIN para registrar credenciales", e);
        }
    }
    
    
    private RegistroDoctor enriquecerConDireccion(RegistroDoctor doctor) {
        if (doctor.getIdDireccion() == null) return doctor;
        try {
            Object dir = webClientBuilder.build().get()
                    .uri("http://localhost:8083/api/v1/direcciones/" + doctor.getIdDireccion())
                    .retrieve()
                    .bodyToMono(Object.class).block();
            doctor.setDatosDireccion(dir);
        } catch (Exception e) {
            doctor.setDatosDireccion(null);
            throw new ComunicacionMicroservicioException("Información de dirección no disponible", e);
        }
        return doctor;
    }

    private void eliminarDireccionEnMS(Long idDireccion) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri("http://localhost:8083/api/v1/direcciones/" + idDireccion)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Considera si quieres lanzar excepción o solo loguear
            throw new ComunicacionMicroservicioException("No se pudo eliminar la dirección asociada al doctor", e);
        }
    }

    private Long crearDireccion(DireccionDTO direccionDTO) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8083/api/v1/direcciones")
                    .bodyValue(direccionDTO)
                    .retrieve()
                    .bodyToMono(Long.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al comunicarse con MS-DIRECCION para crear la dirección del doctor", e);
        }
    }

    private void actualizarDireccion(Long idDireccion, DireccionDTO direccionDTO) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8083/api/v1/direcciones/" + idDireccion)
                    .bodyValue(direccionDTO)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new ComunicacionMicroservicioException("Error al comunicarse con MS-DIRECCION para actualizar la dirección del doctor", e);
        }
    }

    private void eliminarDatosCalendarioEnMS(String runDoctor) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri("http://localhost:8086/api/v1/calendario/agenda/doctor/" + runDoctor)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // En microservicios, decides si esto debe detener el borrado o solo loguear un error
            throw new ComunicacionMicroservicioException("No se pudo limpiar la agenda del doctor en MS-CALENDARIO", e);
        }
    }

}