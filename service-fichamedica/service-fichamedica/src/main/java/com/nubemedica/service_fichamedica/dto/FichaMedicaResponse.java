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
    
    private PacienteDTO datosPaciente; // Datos traídos de MS-Pacientes vía WebClient
    
    private List<ContactoProfesional> contactos;
    private List<TelefonoEmergencia> telefonos;
    private List<FarmacosRecetados> farmacos;
}
