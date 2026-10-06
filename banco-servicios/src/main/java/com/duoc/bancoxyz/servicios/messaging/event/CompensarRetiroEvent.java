package com.duoc.bancoxyz.servicios.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Accion compensatoria de la Saga: el saldo ya se debito pero el cajero
 * no pudo entregar el efectivo, asi que se le pide a ms-cuentas que
 * reintegre el monto.
 */
public record CompensarRetiroEvent(
        String eventId,
        String retiroId,
        Long cuentaId,
        BigDecimal monto,
        String motivo,
        String correlationId,
        OffsetDateTime fecha) {
}
