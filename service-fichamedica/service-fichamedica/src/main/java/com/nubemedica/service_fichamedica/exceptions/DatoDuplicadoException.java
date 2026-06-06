package com.nubemedica.service_fichamedica.exceptions;

public class DatoDuplicadoException extends RuntimeException {
    public DatoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}