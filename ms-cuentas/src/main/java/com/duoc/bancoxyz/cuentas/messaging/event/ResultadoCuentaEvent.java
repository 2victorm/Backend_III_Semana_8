package com.duoc.bancoxyz.cuentas.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** resultado: DEBITADO, RECHAZADO o REVERTIDO. */
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
