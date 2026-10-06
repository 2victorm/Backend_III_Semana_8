package com.duoc.bancoxyz.cuentas.cuenta;

import java.math.BigDecimal;

/**
 * Fila de procesamientos_retiro: lo que ms-cuentas ya hizo por cada
 * retiro y etapa (DEBITO o REVERSO). La PK (retiro_id, etapa) es la
 * garantia de idempotencia: un mismo paso nunca se aplica dos veces.
 */
public record Procesamiento(
        String retiroId,
        String etapa,
        Long cuentaId,
        BigDecimal monto,
        String resultado,
        String motivo,
        BigDecimal saldoResultante,
        String instancia) {

    public static final String DEBITO = "DEBITO";
    public static final String REVERSO = "REVERSO";
}
