package com.nubemedica.service_registrodoctor.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.nubemedica.service_registrodoctor.dto.*;
import com.nubemedica.service_registrodoctor.model.AtencionesDoctor;
import com.nubemedica.service_registrodoctor.model.RegistroDoctor;
import com.nubemedica.service_registrodoctor.repository.AtencionesDoctorRepository;
import com.nubemedica.service_registrodoctor.repository.RegistroDoctorRepository;

import reactor.core.publisher.Mono;
//h
@ExtendWith(MockitoExtension.class)
public class AtencionesDoctorServiceTest {

    @Mock
    private AtencionesDoctorRepository atencionesRepository;

    @Mock
    private RegistroDoctorRepository doctorRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AtencionesDoctorService atencionesService;

    @BeforeEach
    void setUp() {
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void registrarAtencion_Success() {
        AtencionesDoctorDTO dto = new AtencionesDoctorDTO();
        dto.setRunDoctor("1-1");
        dto.setRunPaciente("2-2");

        RegistroDoctor doctor = new RegistroDoctor();
        doctor.setRunDoctor("1-1");

        when(doctorRepository.findByRunDoctor("1-1")).thenReturn(doctor);
        when(atencionesRepository.save(any(AtencionesDoctor.class))).thenAnswer(i -> i.getArguments()[0]);

        AtencionesDoctor result = atencionesService.registrarAtencion(dto);

        assertNotNull(result);
        assertEquals("2-2", result.getRunPaciente());
        assertEquals("1-1", result.getDoctor().getRunDoctor());
    }

    @Test
    void listarPacientesDeDoctor_EnriqueceDatos() {
        String runDoc = "1-1";
        AtencionesDoctor atencion = new AtencionesDoctor();
        atencion.setRunPaciente("paci-123");
        
        RegistroDoctor doc = new RegistroDoctor();
        doc.setIdDireccion(1L);
        atencion.setDoctor(doc);

        when(atencionesRepository.findByDoctorRunDoctor(runDoc)).thenReturn(List.of(atencion));

        // Mock para MS-PACIENTES 
        PacienteResumen resumen = new PacienteResumen();
        resumen.setNombres("Juan");
        
        // Mock para MS-DIRECCIONES 
        DireccionResponse dir = new DireccionResponse();
        dir.setNombre("Alameda");

        mockGet(resumen); // Primero pide paciente, luego dirección (el mock simple devolverá lo mismo o hay que ajustarlo)
        List<AtencionesDoctor> lista = atencionesService.listarPacientesDeDoctor(runDoc);

        assertFalse(lista.isEmpty());
        assertNotNull(lista.get(0).getDatosPaciente());
        verify(atencionesRepository).findByDoctorRunDoctor(runDoc);
    }

    private void mockGet(Object responseBody) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(responseBody));
    }
}