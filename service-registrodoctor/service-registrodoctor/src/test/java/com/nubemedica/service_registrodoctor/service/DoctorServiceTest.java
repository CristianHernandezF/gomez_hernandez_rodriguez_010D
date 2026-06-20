package com.nubemedica.service_registrodoctor.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registrodoctor.dto.*;
import com.nubemedica.service_registrodoctor.exceptions.DatoDuplicadoException;
import com.nubemedica.service_registrodoctor.model.RegistroDoctor;
import com.nubemedica.service_registrodoctor.repository.RegistroDoctorRepository;

import reactor.core.publisher.Mono;
//h
@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private RegistroDoctorRepository doctorRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

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
    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        // Configuración necesaria para que webClientBuilder.build() funcione en cada test
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void registrarDoctor_Success() {
        RegistrarDoctorRequest request = new RegistrarDoctorRequest();
        request.setRunDoctor("123-4");
        request.setCorreo("doc@test.com");
        request.setTelefono("999");
        request.setDireccion(new DireccionRequest());

        when(doctorRepository.existsById(anyString())).thenReturn(false);
        when(doctorRepository.existsByCorreo(anyString())).thenReturn(false);
        when(doctorRepository.existsByTelefono(anyString())).thenReturn(false);

        // Mock para MS-DIRECCIONES
        DireccionResponse dirRes = new DireccionResponse();
        dirRes.setIdDireccion(100L);
        mockPost(dirRes);

        // Mock para MS-LOGIN
        when(doctorRepository.save(any(RegistroDoctor.class))).thenAnswer(i -> i.getArguments()[0]);

        DoctorResponse response = doctorService.registrarDoctor(request);

        assertNotNull(response);
        assertEquals("123-4", response.getRunDoctor());
        verify(doctorRepository, times(1)).save(any());
    }

    @Test
    void registrarDoctor_ThrowsDatoDuplicado() {
        RegistrarDoctorRequest request = new RegistrarDoctorRequest();
        request.setRunDoctor("123-4");
        when(doctorRepository.existsById("123-4")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> doctorService.registrarDoctor(request));
    }

    // Helper para mockear WebClient POST/PUT
    private void mockPost(Object responseBody) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(responseBody));
    }
}