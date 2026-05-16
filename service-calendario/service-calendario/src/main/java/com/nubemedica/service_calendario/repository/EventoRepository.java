package com.nubemedica.service_calendario.repository;

import com.nubemedica.service_calendario.model.Evento;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    
    // Verifica si hay un evento para un doctor en esa fecha y hora
    Optional<Evento> findByRunDoctorAndFechaAndHora(String runDoctor, LocalDate fecha, LocalTime hora);
    
    // Obtiene toda la agenda de un doctor ordenada
    List<Evento> findByRunDoctorOrderByFechaAscHoraAsc(String runDoctor);
    
    // Filtra por tipo de evento (nombreTipo)
    List<Evento> findByRunDoctorAndTipoEvento_NombreTipo(String runDoctor, String nombreTipo);

    @Modifying
    @Transactional
    void deleteByRunDoctor(String runDoctor);
}
