package com.duoc.bancoxyz.servicios.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Paso 1 de la Saga. banco-servicios registro la solicitud y le pide a
 * ms-cuentas que valide y debite el saldo.
 *
 * eventId es unico por mensaje (UUID) para que el consumidor pueda
 * detectar duplicados. retiroId identifica a toda la Saga.
 */
public record RetiroSolicitadoEvent(
                String eventId,
                String retiroId,
                Long cuentaId,
                BigDecimal monto,
                String correlationId,
                OffsetDateTime fecha) {
}
