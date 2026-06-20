package com.nubemedica.service_calendario.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

import com.nubemedica.service_calendario.dto.*;
import com.nubemedica.service_calendario.model.*;
import com.nubemedica.service_calendario.repository.*;
//h
@ExtendWith(MockitoExtension.class)
public class ActividadPersonalServiceTest {

    @Mock 
    private ActividadPersonalRepository actividadPersonalRepository;
    
    @Mock 
    private EventoRepository eventoRepository;
    
    @Mock 
    private TipoEventoRepository tipoEventoRepository;
    
    @Mock 
    private WebClient.Builder webClientBuilder;
    
    @Mock 
    private WebClient webClient;

    // Estos Mocks son necesarios para simular la cadena de WebClient sin que den NullPointerException
    @SuppressWarnings("rawtypes") 
    @Mock 
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @SuppressWarnings("rawtypes") 
    @Mock 
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    
    @SuppressWarnings("rawtypes") 
    @Mock 
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    
    @SuppressWarnings("rawtypes") 
    @Mock 
    private WebClient.RequestBodySpec requestBodySpec;
    
    @Mock 
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks 
    private ActividadPersonalService actividadService;

    @BeforeEach
    void setUp() {
        // Inyectamos las URLs que el Service lee de application.properties (@Value)
        ReflectionTestUtils.setField(actividadService, "urlDoctores", "http://localhost:8081/api/v1/doctores");
        ReflectionTestUtils.setField(actividadService, "urlNotificaciones", "http://localhost:8091/api/v1/notificaciones");
        
        // Configuramos el builder para que devuelva nuestro webClient de prueba
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void crearActividadPersonal_Success() {
        // 1. GIVEN - Preparamos los datos de entrada
        ActividadPersonalRequestDTO request = new ActividadPersonalRequestDTO();
        request.setFecha(LocalDate.now().plusDays(1)); // Mañana para que pase la validación de fecha
        request.setHora(LocalTime.of(10, 0));
        request.setNombreActividad("Congreso Médico");
        request.setDescripcion("Descripción de prueba");

        // Preparamos los modelos falsos
        TipoEvento tipo = new TipoEvento();
        tipo.setNombreTipo("Actividad Personal");

        // Configuramos los repositorios
        when(tipoEventoRepository.findByNombreTipo(anyString())).thenReturn(tipo);
        when(eventoRepository.findByRunDoctorOrderByFechaAscHoraAsc(anyString())).thenReturn(List.of()); // Lista vacía = sin choques de horario
        
        // Mock para obtener datos del doctor (GET)
        DoctorResponseDTO docFake = new DoctorResponseDTO();
        docFake.setCorreo("doctor@test.com");
        docFake.setNombreCompleto("Dr. Simi");
        mockGet(docFake);

        // Mock para la notificación (POST que devuelve Void)
        mockPostVoid();

        // Configuramos el save para que devuelva el mismo objeto que recibe
        when(actividadPersonalRepository.save(any(ActividadPersonal.class))).thenAnswer(i -> {
            ActividadPersonal a = i.getArgument(0);
            a.setIdEvento(1L); // Le asignamos un ID falso
            return a;
        });

        // 2. WHEN - Ejecutamos la lógica real
        ActividadPersonalResponseDTO response = actividadService.crearActividadPersonal(request, "1-1", "token-fake");

        // 3. THEN - Verificamos que todo sea correcto
        assertNotNull(response);
        assertEquals("Congreso Médico", response.getNombreActividad());
        verify(actividadPersonalRepository, times(1)).save(any());
        verify(webClient, atLeastOnce()).post(); // Verifica que se intentó enviar la notificación
    }

    // ======================================
    // HELPERS PARA WEBCLIENT (EVITAN NPE)
    // ======================================

    private void mockGet(DoctorResponseDTO body) {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Respondemos específicamente con el Doctor solo si piden DoctorResponseDTO
        lenient().when(responseSpec.bodyToMono(eq(DoctorResponseDTO.class)))
                 .thenReturn(Mono.just(body));
    }

    private void mockPostVoid() {
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Respondemos con Mono.empty() cuando el servicio pida un Void.class (Notificaciones)
        lenient().when(responseSpec.bodyToMono(eq(Void.class)))
                 .thenReturn(Mono.empty());
    }
}