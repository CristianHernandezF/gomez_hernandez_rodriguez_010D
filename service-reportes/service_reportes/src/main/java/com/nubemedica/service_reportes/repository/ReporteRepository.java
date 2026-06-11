package com.nubemedica.service_reportes.repository;

import com.nubemedica.service_reportes.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByIdFichaMedica(Long idFichaMedica);
}
