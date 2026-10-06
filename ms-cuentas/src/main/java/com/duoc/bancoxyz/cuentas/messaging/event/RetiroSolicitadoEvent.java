package com.duoc.bancoxyz.cuentas.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Copia local del evento (cada microservicio define sus propias clases). */
public record RetiroSolicitadoEvent(
        String eventId,
        String retiroId,
        Long cuentaId,
        BigDecimal monto,
        String correlationId,
        OffsetDateTime fecha) {
}
