package com.duoc.bancoxyz.servicios.repository;

import com.duoc.bancoxyz.servicios.model.TransaccionResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a la tabla transacciones.
 * Esta tabla no tiene cuenta_id, por lo que no existe
 * un metodo buscarPorCuenta.
 */
@Repository
public class TransaccionRepository {

    private static final String COLUMNAS = "id, fecha, monto, tipo";

    private final JdbcClient jdbcClient;

    public TransaccionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<TransaccionResponse> buscarTodas() {
        return jdbcClient.sql("SELECT " + COLUMNAS + " FROM transacciones ORDER BY fecha DESC, id DESC")
                .query(this::mapear)
                .list();
    }

    private TransaccionResponse mapear(ResultSet rs, int rowNum) throws SQLException {
        return new TransaccionResponse(
                rs.getLong("id"),
                rs.getObject("fecha", LocalDate.class),
                rs.getBigDecimal("monto"),
                rs.getString("tipo"));
    }
}
