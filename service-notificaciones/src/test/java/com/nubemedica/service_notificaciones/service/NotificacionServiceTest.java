package com.nubemedica.service_notificaciones.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nubemedica.service_notificaciones.dto.NotificacionRequestDTO;
import com.nubemedica.service_notificaciones.dto.NotificacionResponseDTO;
import com.nubemedica.service_notificaciones.exceptions.RecursoNoEncontradoException;
import com.nubemedica.service_notificaciones.model.Notificacion;
import com.nubemedica.service_notificaciones.repository.NotificacionRepository;

@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    @InjectMocks
    private NotificacionService notificacionService;

    @Test
    void programarNotificacion_Success() {
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        request.setCorreoDestino("test@nubemedica.com");
        request.setAsunto("Recordatorio Cita");
        request.setMensaje("Usted tiene una cita mañana");
        request.setFechaEnvio(LocalDate.now().plusDays(1));
        request.setHoraEnvio(LocalTime.of(10, 0));
        request.setIdEvento(100L);

        when(repository.save(any(Notificacion.class))).thenAnswer(i -> {
            Notificacion n = i.getArgument(0);
            n.setIdNotificacion(1L);
            return n;
        });

        NotificacionResponseDTO response = notificacionService.programarNotificacion(request);

        assertNotNull(response);
        assertEquals(1L, response.getIdNotificacion());
        assertEquals("test@nubemedica.com", response.getCorreoDestino());
        assertFalse(response.isEstadoEnvio()); // Debe iniciar como pendiente
        verify(repository).save(any());
    }

    @Test
    void obtenerPorId_Success() {
        Notificacion notif = new Notificacion();
        notif.setIdNotificacion(1L);
        notif.setCorreoDestino("test@nubemedica.com");

        when(repository.findById(1L)).thenReturn(Optional.of(notif));

        NotificacionResponseDTO response = notificacionService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getIdNotificacion());
        verify(repository).findById(1L);
    }

    @Test
    void obtenerPorId_NotFound_ThrowsException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            notificacionService.obtenerPorId(1L);
        });
    }

    @Test
    void marcarComoEnviada_Success() {
        Notificacion notif = new Notificacion();
        notif.setIdNotificacion(1L);
        notif.setEstadoEnvio(false);

        when(repository.findById(1L)).thenReturn(Optional.of(notif));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificacionService.marcarComoEnviada(1L);

        assertTrue(notif.isEstadoEnvio());
        verify(repository).save(notif);
    }

    @Test
    void eliminarPorEvento_Success() {
        Long idEvento = 100L;
        List<Notificacion> lista = List.of(new Notificacion(), new Notificacion());
        when(repository.findByIdEvento(idEvento)).thenReturn(lista);

        notificacionService.eliminarPorEvento(idEvento);

        verify(repository).deleteAll(lista);
    }

    @Test
    void eliminarNotificacion_NotFound_ThrowsException() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> {
            notificacionService.eliminarNotificacion(1L);
        });
    }
}