package com.nubemedica.service_calendario.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.model.*;
import com.nubemedica.service_calendario.repository.*;
//h
@ExtendWith(MockitoExtension.class)
public class CitaMedicaServiceTest {

    @Mock private CitaMedicaRepository citaMedicaRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private TipoEventoRepository tipoEventoRepository;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;

    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks private CitaMedicaService citaMedicaService;

    @BeforeEach
    void setUp() {
        // Inyectamos todas las URLs de los microservicios
        ReflectionTestUtils.setField(citaMedicaService, "urlAtenciones", "http://atenciones");
        ReflectionTestUtils.setField(citaMedicaService, "urlTelemedicina", "http://tele");
        ReflectionTestUtils.setField(citaMedicaService, "urlEstadoCita", "http://estado");
        ReflectionTestUtils.setField(citaMedicaService, "urlPacientes", "http://pacientes");
        ReflectionTestUtils.setField(citaMedicaService, "urlDoctores", "http://doctores");
        ReflectionTestUtils.setField(citaMedicaService, "urlNotificaciones", "http://notif");
        
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void crearCitaMedica_Success() {
        // GIVEN
        CitaMedicaRequestDTO request = new CitaMedicaRequestDTO();
        request.setFecha(LocalDate.now().plusDays(2));
        request.setHora(LocalTime.of(15, 0));
        request.setRunPaciente("paciente-123");
        request.setMotivoConsulta("Dolor de cabeza");

        // 1. Mock validación relación (GET devuelve Boolean)
        mockGet(Boolean.TRUE, Boolean.class);

        // 2. Mock Telemedicina (POST devuelve TelemedicinaResponseDTO)
        TelemedicinaResponseDTO tele = new TelemedicinaResponseDTO();
        tele.setIdSesionTelemedicina(100L);
        tele.setLinkAcceso("http://link.test");
        mockPost(tele, TelemedicinaResponseDTO.class);

        // 3. Mock Estado Cita (POST devuelve Long)
        mockPost(200L, Long.class);
        
        // 4. Datos para notificaciones y respuesta final
        PacienteDTO pac = new PacienteDTO();
        pac.setCorreo("p@t.com");
        pac.setNombreCompleto("Juan Paciente");
        
        DoctorResponseDTO doc = new DoctorResponseDTO();
        doc.setCorreo("d@t.com");
        doc.setNombreCompleto("Dr. House");
        
        // --- CONFIGURACIÓN DE RESPUESTAS PARA WEBCLIENT ---
        // Esto le dice a Mockito cómo responder a cada GET que hace el Service
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // RESPUESTAS ESPECÍFICAS
        lenient().when(responseSpec.bodyToMono(eq(PacienteDTO.class))).thenReturn(Mono.just(pac));
        lenient().when(responseSpec.bodyToMono(eq(DoctorResponseDTO.class))).thenReturn(Mono.just(doc));
        lenient().when(responseSpec.bodyToMono(eq(EstadoCitaMedicaDTO.class))).thenReturn(Mono.just(new EstadoCitaMedicaDTO("Agendada", "Test")));
        lenient().when(responseSpec.bodyToMono(eq(TelemedicinaResponseDTO.class))).thenReturn(Mono.just(tele));
        lenient().when(responseSpec.bodyToMono(eq(Void.class))).thenReturn(Mono.empty());

        // Mock del repositorio
        when(citaMedicaRepository.save(any())).thenAnswer(i -> {
            CitaMedica c = i.getArgument(0);
            c.setIdEvento(1L);
            c.setIdEstadoCitaMedica(200L);
            c.setIdSesionTelemedicina(100L);
            return c;
        });

        CitaMedicaResponseDTO response = citaMedicaService.crearCitaMedica(request, "doc-1", "token-xyz");

        assertNotNull(response);
        assertEquals("Dolor de cabeza", response.getMotivoConsulta());
        verify(citaMedicaRepository).save(any());
    }


    // HELPERS ESPECÍFICOS
     private void mockGet(Object body, Class<?> clazz) {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(eq(clazz))).thenReturn((Mono) Mono.just(body));
    }

    private void mockPost(Object body, Class<?> clazz) {
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        // Permitimos que el retrieve venga desde el body o desde los headers
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(eq(clazz))).thenReturn((Mono) Mono.just(body));
    }
}