package com.nubemedica.service_calendario.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nubemedica.service_calendario.model.*;
import com.nubemedica.service_calendario.repository.*;

@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private CitaMedicaRepository citaMedicaRepository;

    @InjectMocks private EventoService eventoService;

    @Test
    void eliminarTodaLaAgenda_Success() {
        String runDoc = "1-1";
        CitaMedica cita = new CitaMedica();
        cita.setIdEstadoCitaMedica(500L);
        
        when(citaMedicaRepository.findByRunDoctor(runDoc)).thenReturn(List.of(cita));

        eventoService.eliminarTodaLaAgendaDelDoctor(runDoc);

        verify(citaMedicaRepository).findByRunDoctor(runDoc);
        verify(eventoRepository).deleteByRunDoctor(runDoc);
    }
}