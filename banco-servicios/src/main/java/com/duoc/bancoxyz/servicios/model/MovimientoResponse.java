package com.duoc.bancoxyz.servicios.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Detalle de movimientos por cuenta, tabla "cuentas_anuales" (Job 3). */
public record MovimientoResponse(
        Long cuentaId,
        LocalDate fecha,
        String transaccion,
        BigDecimal monto,
        String descripcion) {
}
