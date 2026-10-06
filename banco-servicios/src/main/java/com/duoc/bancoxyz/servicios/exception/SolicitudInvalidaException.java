package com.duoc.bancoxyz.servicios.exception;

/**
 * Datos de entrada invalidos que no cubre @Valid (ej. un header). Se traduce a
 * 400.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
