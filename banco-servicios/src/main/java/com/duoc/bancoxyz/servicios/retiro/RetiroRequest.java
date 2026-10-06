package com.duoc.bancoxyz.servicios.retiro;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Cuerpo de POST /api/retiros.
 *
 * simularFallaCajero: solo para la demo. Si es true, el cajero "no
 * entrega" el efectivo despues del debito y se dispara el paso
 * compensatorio de la Saga.
 */
public record RetiroRequest(
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser positivo") Long cuentaId,
        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "1", message = "debe ser al menos 1")
        @Digits(integer = 12, fraction = 2, message = "maximo 12 enteros y 2 decimales") BigDecimal monto,
        Boolean simularFallaCajero) {

    public boolean fallaCajero() {
        return Boolean.TRUE.equals(simularFallaCajero);
    }
}
