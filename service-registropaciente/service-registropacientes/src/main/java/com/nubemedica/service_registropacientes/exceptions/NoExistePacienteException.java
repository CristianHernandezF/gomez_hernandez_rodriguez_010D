package com.nubemedica.service_registropacientes.exceptions;

public class NoExistePacienteException extends RuntimeException{

    public NoExistePacienteException(String runPaciente){
        super("Paciente con RUN" +runPaciente+ "No encontrado");
    }

}
