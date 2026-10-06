package com.duoc.bancoxyz.servicios.model;

import java.math.BigDecimal;

/** Resumen anual por cuenta, tabla "estados_cuenta_anuales" (Job 3). */
public record EstadoCuentaResponse(
        Long cuentaId,
        Integer anio,
        Integer cantidadMovimientos,
        BigDecimal totalDepositos,
        BigDecimal totalRetiros,
        BigDecimal totalCompras,
        BigDecimal saldoNeto) {
}
