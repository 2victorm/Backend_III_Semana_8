package com.duoc.bancoxyz.servicios.retiro;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Respuesta de /api/retiros: el estado actual de la Saga. */
public record RetiroResponse(
        String retiroId,
        Long cuentaId,
        BigDecimal monto,
        EstadoRetiro estado,
        String motivo,
        String mensaje,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn) {

    public static RetiroResponse de(SolicitudRetiro s, String mensaje) {
        return new RetiroResponse(s.id(), s.cuentaId(), s.monto(), s.estado(), s.motivo(), mensaje,
                s.creadoEn(), s.actualizadoEn());
    }
}
