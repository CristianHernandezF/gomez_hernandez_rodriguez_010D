package com.nubemedica.service_fichamedica.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FichaMedicaUpdateRequest {

    @NotBlank(message = "El historial familiar es obligatorio para guardar la atención")
    private String historialFamiliar;

    @NotBlank(message = "La hipótesis diagnóstica es obligatoria")
    private String hipotesisDiagnostica;

    @Valid // Valida cada elemento de la lista
    private List<ContactoProfesionalDTO> contactosProfesionales;

    @Valid
    private List<TelefonoEmergenciaDTO> telefonosEmergencia;

    @Valid
    private List<FarmacosRecetadosDTO> farmacos;
}
