package com.nubemedica.service_registropacientes.dto;

import com.nubemedica.service_registropacientes.model.Paciente;

public record PacienteRegistroResponse(
        Paciente paciente,
        String mensaje,
        boolean yaExistia) {}
