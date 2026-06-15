package com.nubemedica.service_notificaciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_notificaciones.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByIdEvento(Long idEvento);

}
