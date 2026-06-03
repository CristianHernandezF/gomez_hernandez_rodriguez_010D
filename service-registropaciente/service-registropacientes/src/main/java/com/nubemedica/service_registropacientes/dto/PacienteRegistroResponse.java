package com.nubemedica.service_registropacientes.dto;


public record PacienteRegistroResponse(
        PacienteResponse paciente,
        String mensaje,
        boolean yaExistia) {}
