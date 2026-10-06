package com.duoc.bancoxyz.servicios.exception;

/**
 * Se lanza desde los metodos de fallback cuando el Circuit Breaker
 * detecta que la base de datos no esta respondiendo.
 *
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
