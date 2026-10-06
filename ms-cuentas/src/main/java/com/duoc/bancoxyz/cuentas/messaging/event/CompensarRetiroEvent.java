package com.duoc.bancoxyz.cuentas.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CompensarRetiroEvent(
        String eventId,
        String retiroId,
        Long cuentaId,
        BigDecimal monto,
        String motivo,
        String correlationId,
        OffsetDateTime fecha) {
}
