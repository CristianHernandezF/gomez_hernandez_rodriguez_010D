package com.nubemedica.service_registrodoctor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nubemedica.service_registrodoctor.model.RegistroDoctor;

@Repository
public interface RegistroDoctorRepository extends JpaRepository<RegistroDoctor, String> {

    RegistroDoctor findByRunDoctor(String runDoctor);

    void deleteByRunDoctor(String runDoctor);

    boolean existsByTelefono(String telefono);

    boolean existsByCorreo(String correo); 
    
    RegistroDoctor findByCorreo(String correo);

}
