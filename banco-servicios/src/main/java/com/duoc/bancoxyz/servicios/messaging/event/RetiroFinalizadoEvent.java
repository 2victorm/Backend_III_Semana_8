package com.duoc.bancoxyz.servicios.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Cierre de la Saga, publicado en el topico para que cualquier servicio
 * interesado (notificaciones, auditoria) reaccione sin que banco-servicios
 * sepa quienes son. estadoFinal: APROBADA, RECHAZADA o REVERTIDA.
 */
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
