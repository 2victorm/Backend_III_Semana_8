package com.duoc.bancoxyz.servicios.exception;

/** Se traduce a 404 en el GlobalExceptionHandler. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
