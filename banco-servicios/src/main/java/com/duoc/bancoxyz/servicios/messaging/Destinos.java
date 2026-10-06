package com.duoc.bancoxyz.servicios.messaging;

/**
 * Nombres de las 3 colas y del tópico.
 *
 * banco.<agregado>.<que paso>
 */
public final class Destinos {

    /** Cola (punto a punto): banco-servicios -> ms-cuentas. */
    public static final String COLA_RETIROS_SOLICITADOS = "banco.retiros.solicitados";

    /** Cola (punto a punto): ms-cuentas -> banco-servicios. */
    public static final String COLA_RETIROS_RESULTADO = "banco.retiros.resultado";

    /** Cola (punto a punto): banco-servicios -> ms-cuentas, paso compensatorio. */
    public static final String COLA_RETIROS_COMPENSACION = "banco.retiros.compensacion";

    /**
     * Topico (publicacion-suscripcion): banco-servicios -> todos los interesados.
     */
    public static final String TOPICO_RETIROS_EVENTOS = "banco.retiros.eventos";

    private Destinos() {
    }
}
