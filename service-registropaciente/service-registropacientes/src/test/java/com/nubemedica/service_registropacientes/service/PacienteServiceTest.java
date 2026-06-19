package com.nubemedica.service_registropacientes.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.nubemedica.service_registropacientes.dto.*;
import com.nubemedica.service_registropacientes.exceptions.*;
import com.nubemedica.service_registropacientes.model.Paciente;
import com.nubemedica.service_registropacientes.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
public class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PacienteService pacienteService;

    private final String urlMsFichamedica = "http://localhost:8088/api/v1/fichas";

    @BeforeEach
    void setUp() {
        // Configuramos la inyección del campo @Value manualmente
        ReflectionTestUtils.setField(pacienteService, "urlMsFichamedica", urlMsFichamedica);
        
        // Configuramos el builder para que devuelva siempre nuestro webClient mockeado
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void guardarPaciente_Nuevo_Success() {
        PacienteRegistroRequest request = new PacienteRegistroRequest();
        request.setRunPaciente("123-K");
        request.setCorreo("paciente@test.com");
        request.setNumTelefono("912345678");
        request.setPriNombre("Juan");
        request.setApaPaterno("Perez");
        request.setApaMaterno("Gomez");
        request.setDireccion(new DireccionRequest());

        when(pacienteRepository.findById(anyString())).thenReturn(Optional.empty());
        when(pacienteRepository.existsByCorreo(anyString())).thenReturn(false);
        when(pacienteRepository.existsByNumTelefono(anyString())).thenReturn(false);
        
        // 1. Mock MS-DIRECCIONES
        DireccionResponse dirRes = new DireccionResponse();
        dirRes.setIdDireccion(1L);
        mockPost(dirRes);

        // 2. Mock MS-DOCTORES y MS-FICHAMEDICA
        lenient().when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArguments()[0]);

        PacienteRegistroResponse response = pacienteService.guardarPaciente(request, "DOC-1");

        assertNotNull(response);
        assertFalse(response.yaExistia()); // Uso de record syntax
        assertEquals("Paciente registrado y asociado exitosamente.", response.mensaje());
        verify(pacienteRepository).save(any());
    }

    @Test
    void guardarPaciente_YaExiste_AsociaExitosamente() {
        PacienteRegistroRequest request = new PacienteRegistroRequest();
        request.setRunPaciente("123-K");
        
        Paciente existente = new Paciente();
        existente.setRunPaciente("123-K");
        existente.setPriNombre("Juan");
        existente.setApaPaterno("Perez");
        existente.setApaMaterno("Gomez");

        when(pacienteRepository.findById("123-K")).thenReturn(Optional.of(existente));
        
        // Mock para las llamadas de asociación y ficha
        mockPost(null); 

        PacienteRegistroResponse response = pacienteService.guardarPaciente(request, "DOC-1");

        assertTrue(response.yaExistia());
        assertTrue(response.mensaje().contains("ya existe"));
    }

    @Test
    void obtenerPacientePorRun_AccesoDenegado() {
        mockGet(false); // Simula que el microservicio de doctores devuelve false (no hay relacion)
        assertThrows(AccesoDenegadoException.class, () -> {
            pacienteService.obtenerPacientePorRun("123", "DOC-1");
        });
    }

    @Test
    void asociarYCrearFicha_FallaFicha_RevierteAsociacion() {
        // 1. GIVEN - Preparamos el Request con datos completos
        PacienteRegistroRequest request = new PacienteRegistroRequest();
        request.setRunPaciente("123-K");
        request.setCorreo("error@test.com");
        request.setNumTelefono("999999");
        request.setPriNombre("Juan"); // Asegura estos campos para el mapeador
        request.setApaPaterno("Perez");
        request.setApaMaterno("Gomez");
        request.setDireccion(new DireccionRequest());

        // 2. Mocks del Repositorio
        when(pacienteRepository.findById(anyString())).thenReturn(Optional.empty());
        when(pacienteRepository.existsByCorreo(anyString())).thenReturn(false);
        when(pacienteRepository.existsByNumTelefono(anyString())).thenReturn(false);
        
        // IMPORTANTE: Mockear el save para que NO devuelva null
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArguments()[0]);

        // 3. Mocks del WebClient (Estructura de la petición)
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(webClient.method(any())).thenReturn(requestBodyUriSpec); // Para el DELETE
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodyUriSpec.uri(anyString(), any(), any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // 4. Mocks de las respuestas
        // Respuesta para la Dirección 
        DireccionResponse dir = new DireccionResponse();
        dir.setIdDireccion(1L);
        lenient().when(responseSpec.bodyToMono(DireccionResponse.class)).thenReturn(Mono.just(dir));
        // Respuestas para Void (Asociación, Ficha Médica y Desasociación)
        lenient().when(responseSpec.bodyToMono(Void.class))
            .thenReturn(Mono.empty()) // 1. Asociación: OK
            .thenReturn(Mono.error(new RuntimeException("Fallo Ficha"))) // 2. Ficha: ERROR
            .thenReturn(Mono.empty()); // 3. Desasociación (Compensación): OK
        // 5. WHEN & THEN
        assertThrows(ComunicacionMicroservicioException.class, () -> {
            pacienteService.guardarPaciente(request, "DOC-1");
        });

        // 6. Verificación final: Que se haya intentado borrar la asociación
        verify(webClient).method(eq(org.springframework.http.HttpMethod.DELETE));
    }

    // --- HELPERS PARA WEBCLIENT ---

    private void mockPost(Object body) {
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodyUriSpec.uri(anyString(), any(), any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        if (body != null) {
            lenient().when(responseSpec.bodyToMono(any(Class.class)))
                     .thenReturn((Mono) Mono.just(body));
        } else {
            lenient().when(responseSpec.bodyToMono(any(Class.class)))
                     .thenReturn(Mono.empty());
        }
        
        // Muy importante para las llamadas que no devuelven nada
        lenient().when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
    }

    private void mockGet(Object body) {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(any(Class.class))).thenReturn(body != null ? Mono.just(body) : Mono.empty());
    }
}