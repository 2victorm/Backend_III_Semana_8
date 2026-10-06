package com.duoc.bancoxyz.servicios.model;

import java.math.BigDecimal;

/**
 * Datos de la tabla "intereses_calculados" (Job 2). Es la fuente con mayor
 * cobertura de cuentas, y "saldoFinal" se usa como el saldo actual de la
 * cuenta (tomando la fila mas reciente por cuentaId, ver InteresRepository).
 */
public record InteresResponse(
        Long id,
        Long cuentaId,
        String nombre,
        String tipo,
        BigDecimal saldoInicial,
        BigDecimal tasaAplicada,
        BigDecimal interesMensual,
        BigDecimal saldoFinal) {
}
