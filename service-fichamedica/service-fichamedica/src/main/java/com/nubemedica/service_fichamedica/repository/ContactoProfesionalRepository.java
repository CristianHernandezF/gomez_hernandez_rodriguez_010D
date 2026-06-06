package com.nubemedica.service_fichamedica.repository;

import com.nubemedica.service_fichamedica.model.ContactoProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactoProfesionalRepository extends JpaRepository<ContactoProfesional, Long> {
}