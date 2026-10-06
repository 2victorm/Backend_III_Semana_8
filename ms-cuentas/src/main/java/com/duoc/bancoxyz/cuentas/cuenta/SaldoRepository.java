package com.duoc.bancoxyz.cuentas.cuenta;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Tabla saldos_cuenta (propia de ms-cuentas).
 *
 * Aunque haya varias instancias de ms-cuentas procesando retiros de la
 * misma cuenta al mismo tiempo, el saldo nunca queda negativo ni se pierde
 * un descuento.
 */
@Repository
public class SaldoRepository {

    private final JdbcClient jdbcClient;

    public SaldoRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<BigDecimal> buscarSaldo(Long cuentaId) {
        return jdbcClient.sql("SELECT saldo FROM saldos_cuenta WHERE cuenta_id = :cuentaId")
                .param("cuentaId", cuentaId)
                .query(BigDecimal.class)
                .optional();
    }

    /** @return el saldo nuevo, o vacio si la cuenta no existe o no alcanza. */
    public Optional<BigDecimal> debitarSiAlcanza(Long cuentaId, BigDecimal monto) {
        return jdbcClient.sql("""
                UPDATE saldos_cuenta SET saldo = saldo - :monto, actualizado_en = now()
                WHERE cuenta_id = :cuentaId AND saldo >= :monto
                RETURNING saldo
                """)
                .param("cuentaId", cuentaId)
                .param("monto", monto)
                .query(BigDecimal.class)
                .optional();
    }

    /** Paso compensatorio: reintegra el monto. */
    public Optional<BigDecimal> acreditar(Long cuentaId, BigDecimal monto) {
        return jdbcClient.sql("""
                UPDATE saldos_cuenta SET saldo = saldo + :monto, actualizado_en = now()
                WHERE cuenta_id = :cuentaId
                RETURNING saldo
                """)
                .param("cuentaId", cuentaId)
                .param("monto", monto)
                .query(BigDecimal.class)
                .optional();
    }
}
