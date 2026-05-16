package com.nubemedica.service_registrodoctor.exceptions;


public class NoExisteDoctorException extends RuntimeException {

    public NoExisteDoctorException(String runDoctor) {
        super("Doctor con RUN " + runDoctor + " no encontrado");
    }
}
