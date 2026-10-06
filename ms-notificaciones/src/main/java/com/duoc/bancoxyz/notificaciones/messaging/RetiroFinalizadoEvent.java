package com.duoc.bancoxyz.notificaciones.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Copia local del evento publicado por banco-servicios en el topico. */
public record RetiroFinalizadoEvent(
        String eventId,
        String retiroId,
        Long cuentaId,
        BigDecimal monto,
        String estadoFinal,
        String motivo,
        String correlationId,
        OffsetDateTime fecha) {
}
