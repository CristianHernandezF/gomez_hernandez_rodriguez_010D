package com.nubemedica.service_fichamedica.exceptions;

public class ComunicacionMicroservicioException extends RuntimeException {
    public ComunicacionMicroservicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
