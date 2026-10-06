package com.duoc.bancoxyz.servicios.retiro;

/**
 * Estados de una solicitud de retiro a lo largo de la Saga.
 *
 * REGISTRADA  -> guardada en BD, aun no llega al broker
 * ENVIADA     -> RetiroSolicitado publicado, esperando a ms-cuentas
 * APROBADA    -> saldo debitado y efectivo entregado (fin feliz)
 * RECHAZADA   -> ms-cuentas no debito (saldo insuficiente / cuenta inexistente)
 * EN_COMPENSACION -> se debito, pero el cajero fallo; se pidio reintegrar
 * REVERTIDA   -> ms-cuentas reintegro el saldo (fin compensado)
 */
public enum EstadoRetiro {
    REGISTRADA,
    ENVIADA,
    APROBADA,
    RECHAZADA,
    EN_COMPENSACION,
    REVERTIDA
}
