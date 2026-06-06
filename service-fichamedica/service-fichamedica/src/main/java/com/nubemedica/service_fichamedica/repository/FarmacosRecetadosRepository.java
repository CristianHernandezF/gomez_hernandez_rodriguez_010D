package com.nubemedica.service_fichamedica.repository;

import com.nubemedica.service_fichamedica.model.FarmacosRecetados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmacosRecetadosRepository extends JpaRepository<FarmacosRecetados, Long> {
    // Aquí podrías agregar métodos como findByNombreFarmaco si fuera necesario
}
