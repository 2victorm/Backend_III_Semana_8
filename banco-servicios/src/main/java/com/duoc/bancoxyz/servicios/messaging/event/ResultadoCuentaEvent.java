package com.duoc.bancoxyz.servicios.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Respuesta de ms-cuentas. El campo resultado puede ser:
 * DEBITADO (paso 2 ok), RECHAZADO (saldo insuficiente o cuenta
 * inexistente) o REVERTIDO (la compensacion se aplico).
 */
public record ResultadoCuentaEvent(
        String eventId,
        String retiroId,
        Long cuentaId,
        BigDecimal monto,
        String resultado,
        String motivo,
        BigDecimal saldoActual,
        String correlationId,
        OffsetDateTime fecha) {

    public static final String DEBITADO = "DEBITADO";
    public static final String RECHAZADO = "RECHAZADO";
    public static final String REVERTIDO = "REVERTIDO";
}
