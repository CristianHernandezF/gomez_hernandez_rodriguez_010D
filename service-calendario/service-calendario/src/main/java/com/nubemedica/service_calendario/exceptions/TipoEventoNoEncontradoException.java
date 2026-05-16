package com.nubemedica.service_calendario.exceptions;

public class TipoEventoNoEncontradoException extends RuntimeException{

    public TipoEventoNoEncontradoException(String mensaje){
        super(mensaje);
    }

}
