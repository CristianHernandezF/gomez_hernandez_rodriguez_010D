package com.nubemedica.service_registropacientes.exceptions;

public class ComunicacionMicroservicioException extends RuntimeException{

    public ComunicacionMicroservicioException (String mensaje, Throwable causa){
        super(mensaje, causa);
    }

}