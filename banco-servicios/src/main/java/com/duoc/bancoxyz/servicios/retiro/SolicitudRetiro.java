package com.duoc.bancoxyz.servicios.retiro;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Fila de la tabla solicitudes_retiro (propia de banco-servicios). */
public record SolicitudRetiro(
        String id,
        String idempotencyKey,
        Long cuentaId,
        BigDecimal monto,
        boolean simularFallaCajero,
        EstadoRetiro estado,
        String motivo,
        String correlationId,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn) {
}
