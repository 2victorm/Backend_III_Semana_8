package com.duoc.bancoxyz.servicios.repository;

import com.duoc.bancoxyz.servicios.model.EstadoCuentaResponse;
import com.duoc.bancoxyz.servicios.model.MovimientoResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a dos tablas relacionadas: estados_cuenta_anuales y
 * cuentas_anuales.
 * Ambas compoarten cuenta_id como llave de filtro.
 */
@Repository
public class EstadoCuentaRepository {

    private static final String COLUMNAS_ESTADO = "cuenta_id, anio, cantidad_movimientos, total_depositos, total_retiros, total_compras, saldo_neto";

    private static final String COLUMNAS_MOVIMIENTO = "cuenta_id, fecha, transaccion, monto, descripcion";

    private final JdbcClient jdbcClient;

    public EstadoCuentaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<EstadoCuentaResponse> buscarTodos() {
        return jdbcClient
                .sql("SELECT " + COLUMNAS_ESTADO + " FROM estados_cuenta_anuales ORDER BY cuenta_id, anio DESC")
                .query(this::mapearEstado)
                .list();
    }

    public List<EstadoCuentaResponse> buscarPorCuenta(Long cuentaId) {
        return jdbcClient.sql("SELECT " + COLUMNAS_ESTADO
                + " FROM estados_cuenta_anuales WHERE cuenta_id = :cuentaId ORDER BY anio DESC")
                .param("cuentaId", cuentaId)
                .query(this::mapearEstado)
                .list();
    }

    /**
     * Devuelve el detalle linea por linea detras del resumen anual.
     * Cada movimiento individual de la cuenta.
     */
    public List<MovimientoResponse> buscarMovimientosPorCuenta(Long cuentaId) {
        return jdbcClient.sql("SELECT " + COLUMNAS_MOVIMIENTO
                + " FROM cuentas_anuales WHERE cuenta_id = :cuentaId ORDER BY fecha DESC")
                .param("cuentaId", cuentaId)
                .query(this::mapearMovimiento)
                .list();
    }

    private EstadoCuentaResponse mapearEstado(ResultSet rs, int rowNum) throws SQLException {
        return new EstadoCuentaResponse(
                rs.getLong("cuenta_id"),
                rs.getInt("anio"),
                rs.getInt("cantidad_movimientos"),
                rs.getBigDecimal("total_depositos"),
                rs.getBigDecimal("total_retiros"),
                rs.getBigDecimal("total_compras"),
                rs.getBigDecimal("saldo_neto"));
    }

    private MovimientoResponse mapearMovimiento(ResultSet rs, int rowNum) throws SQLException {
        return new MovimientoResponse(
                rs.getLong("cuenta_id"),
                rs.getObject("fecha", LocalDate.class),
                rs.getString("transaccion"),
                rs.getBigDecimal("monto"),
                rs.getString("descripcion"));
    }
}
