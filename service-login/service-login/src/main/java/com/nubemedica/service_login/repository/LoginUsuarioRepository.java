package com.nubemedica.service_login.repository;

import com.nubemedica.service_login.model.LoginUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoginUsuarioRepository extends JpaRepository<LoginUsuario, Long> {
    Optional<LoginUsuario> findByCorreo(String correo);
    Optional<LoginUsuario> findByNumTelefono(String numTelefono);
    Optional<LoginUsuario> findByRunDoctor(String runDoctor);
}