package com.nubemedica.service_fichamedica.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.nubemedica.service_fichamedica.dto.*;
import com.nubemedica.service_fichamedica.model.*;
import com.nubemedica.service_fichamedica.repository.FichaMedicaRepository;

@ExtendWith(MockitoExtension.class)
public class FichaMedicaServiceTest {

    @Mock private FichaMedicaRepository fichaMedicaRepository;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;

    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @SuppressWarnings("rawtypes") @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks private FichaMedicaService fichaMedicaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fichaMedicaService, "urlMsPacientes", "http://pacientes");
        ReflectionTestUtils.setField(fichaMedicaService, "urlMsReportes", "http://reportes");
        ReflectionTestUtils.setField(fichaMedicaService, "urlMsDoctores", "http://atenciones");

        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void crearFicha_Success() {
        String runP = "123-K";
        String runD = "DOC-1";

        // Mocks iniciales
        when(fichaMedicaRepository.existsByRunPacienteAndRunDoctor(runP, runD)).thenReturn(false);
        
        // Mock WebClient para validación relación y datos paciente
        mockGet(Boolean.TRUE, Boolean.class);
        
        PacienteDTO pac = new PacienteDTO();
        pac.setRunPaciente(runP);
        pac.setNombreCompleto("Juan Perez");

        // Respuestas específicas para que no choquen
        lenient().when(responseSpec.bodyToMono(eq(Boolean.class))).thenReturn(Mono.just(true));
        lenient().when(responseSpec.bodyToMono(eq(PacienteDTO.class))).thenReturn(Mono.just(pac));
        lenient().when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(new ArrayList<>()));

        when(fichaMedicaRepository.save(any(FichaMedica.class))).thenAnswer(i -> {
            FichaMedica f = i.getArgument(0);
            f.setIdFichaMedica(1L);
            return f;
        });

        FichaMedicaResponse response = fichaMedicaService.crearFicha(runP, runD);

        assertNotNull(response);
        assertEquals(1L, response.getIdFichaMedica());
    }

    @Test
    void actualizarFicha_Success() {
        Long idFicha = 1L;
        FichaMedica ficha = new FichaMedica();
        ficha.setIdFichaMedica(idFicha);
        ficha.setRunPaciente("123-K");
        ficha.setContactoPro(new ArrayList<>());
        ficha.setTelefonos(new ArrayList<>());
        ficha.setFarmacos(new ArrayList<>());

        FichaMedicaUpdateRequest request = new FichaMedicaUpdateRequest();
        request.setHipotesisDiagnostica("Gripe");
        
        FarmacosRecetadosDTO farmacoDTO = new FarmacosRecetadosDTO();
        farmacoDTO.setNombreFarmaco("Paracetamol");
        farmacoDTO.setDosis(500);
        request.setFarmacos(List.of(farmacoDTO));

        when(fichaMedicaRepository.findById(idFicha)).thenReturn(Optional.of(ficha));
        
        // Mocks WebClient necesarios
        mockGet(Boolean.TRUE, Boolean.class);
        lenient().when(responseSpec.bodyToMono(eq(PacienteDTO.class))).thenReturn(Mono.just(new PacienteDTO()));
        lenient().when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(new ArrayList<>()));

        // Hacemos que el save devuelva la misma ficha que recibió
        when(fichaMedicaRepository.save(any(FichaMedica.class))).thenAnswer(i -> i.getArgument(0));

        FichaMedicaResponse response = fichaMedicaService.actualizarFicha(idFicha, request, "DOC-1", "token");

        assertNotNull(response);
        assertEquals("Gripe", response.getHipotesisDiagnostica());
        // Verificamos que la lista de fármacos NO esté vacía
        assertNotNull(response.getFarmacos());
        assertFalse(response.getFarmacos().isEmpty(), "La lista de fármacos no debería estar vacía");
        assertEquals("Paracetamol", response.getFarmacos().get(0).getNombreFarmaco());
    }

    private void mockGet(Object body, Class<?> clazz) {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        // Soporta .uri con múltiples argumentos
        lenient().when(requestHeadersUriSpec.uri(anyString(), any(), any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        
        //Acepta null en el valor del header (any() en vez de anyString())
        lenient().when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(eq(clazz))).thenReturn((Mono) Mono.just(body));
    }

    private void mockPost(Object body, Class<?> clazz) {
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(eq(clazz))).thenReturn((Mono) Mono.just(body));
    }
}