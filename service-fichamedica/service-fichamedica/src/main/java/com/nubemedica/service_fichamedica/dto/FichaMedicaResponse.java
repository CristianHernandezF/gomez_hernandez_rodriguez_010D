package com.nubemedica.service_fichamedica.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FichaMedicaResponse {

    private Long idFichaMedica;
    private String historialFamiliar;
    private String hipotesisDiagnostica;
    private String runPaciente;
    private String runDoctor;

    private PacienteDTO datosPaciente;

    private List<ContactoProfesionalDTO> contactos;
    private List<TelefonoEmergenciaDTO> telefonos;
    private List<FarmacosRecetadosDTO> farmacos;

    // NUEVO — reportes cargados desde ms-reportes via WebClient
    private List<ReporteDTO> reportes;
}
