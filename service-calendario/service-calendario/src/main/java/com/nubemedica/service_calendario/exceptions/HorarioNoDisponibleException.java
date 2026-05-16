package com.nubemedica.service_calendario.exceptions;

public class HorarioNoDisponibleException extends RuntimeException{

    public HorarioNoDisponibleException(String mensaje){
        super(mensaje);
    }

}
