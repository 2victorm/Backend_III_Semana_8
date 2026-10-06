package com.duoc.bancoxyz.cuentas.messaging;

/** Mismos nombres que en banco-servicios (ver diagrama de arquitectura). */
public final class Destinos {

    public static final String COLA_RETIROS_SOLICITADOS = "banco.retiros.solicitados";
    public static final String COLA_RETIROS_RESULTADO = "banco.retiros.resultado";
    public static final String COLA_RETIROS_COMPENSACION = "banco.retiros.compensacion";

    private Destinos() {
    }
}
