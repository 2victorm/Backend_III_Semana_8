package com.duoc.bancoxyz.servicios.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de la tabla "transacciones" (Job 1). No tiene cuenta_id: es un
 * agregado a nivel de todo el banco, no atribuible a una cuenta especifica.
 */
public record TransaccionResponse(
        Long id,
        LocalDate fecha,
        BigDecimal monto,
        String tipo) {
}
