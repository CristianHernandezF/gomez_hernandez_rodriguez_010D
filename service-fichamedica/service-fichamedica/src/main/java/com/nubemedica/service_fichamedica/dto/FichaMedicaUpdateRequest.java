package com.nubemedica.service_fichamedica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class FichaMedicaUpdateRequest {

    @NotBlank(message = "El historial familiar es obligatorio para guardar la atención")
    private String historialFamiliar;

    @NotBlank(message = "La hipótesis diagnóstica es obligatoria")
    private String hipotesisDiagnostica;

    @Valid // Valida cada elemento de la lista
    private List<ContactoProfesional> contactosProfesionales;

    @Valid
    private List<TelefonoEmergencia> telefonosEmergencia;

    @Valid
    private List<FarmacosRecetados> farmacos;
}
