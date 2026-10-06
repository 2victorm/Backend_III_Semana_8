package com.duoc.bancoxyz.cuentas.cuenta;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class ProcesamientoRepository {

    private static final String COLUMNAS = "retiro_id, etapa, cuenta_id, monto, resultado, motivo, saldo_resultante, instancia";

    private final JdbcClient jdbcClient;

    public ProcesamientoRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Procesamiento> buscar(String retiroId, String etapa) {
        return jdbcClient.sql("SELECT " + COLUMNAS + " FROM procesamientos_retiro WHERE retiro_id = :id AND etapa = :etapa")
                .param("id", retiroId)
                .param("etapa", etapa)
                .query(this::mapear)
                .optional();
    }

    /** Si otra instancia ya inserto la misma (retiro, etapa), lanza DuplicateKeyException. */
    public void insertar(Procesamiento p) {
        jdbcClient.sql("""
                INSERT INTO procesamientos_retiro
                    (retiro_id, etapa, cuenta_id, monto, resultado, motivo, saldo_resultante, instancia)
                VALUES (:retiroId, :etapa, :cuentaId, :monto, :resultado, :motivo, :saldo, :instancia)
                """)
                .param("retiroId", p.retiroId())
                .param("etapa", p.etapa())
                .param("cuentaId", p.cuentaId())
                .param("monto", p.monto())
                .param("resultado", p.resultado())
                .param("motivo", p.motivo())
                .param("saldo", p.saldoResultante())
                .param("instancia", p.instancia())
                .update();
    }

    private Procesamiento mapear(ResultSet rs, int rowNum) throws SQLException {
        return new Procesamiento(
                rs.getString("retiro_id"),
                rs.getString("etapa"),
                rs.getLong("cuenta_id"),
                rs.getBigDecimal("monto"),
                rs.getString("resultado"),
                rs.getString("motivo"),
                rs.getBigDecimal("saldo_resultante"),
                rs.getString("instancia"));
    }
}
