package com.duoc.bancoxyz.servicios.repository;

import com.duoc.bancoxyz.servicios.model.InteresResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a la tabla intereses_calculados.
 * Es la fuente con mayor cobertura de cuentas del proyecto, por
 * eso se eligio como fuente del saldo actual.
 */
@Repository
public class InteresRepository {

    private static final String COLUMNAS = "id, cuenta_id, nombre, tipo, saldo_inicial, tasa_aplicada, interes_mensual, saldo_final";

    private final JdbcClient jdbcClient;

    public InteresRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<InteresResponse> buscarTodos() {
        return jdbcClient.sql("SELECT " + COLUMNAS + " FROM intereses_calculados ORDER BY cuenta_id, id DESC")
                .query(this::mapear)
                .list();
    }

    /**
     * Ordenada por id DESC ya que la tabla no tiene la columna de fecha/mes.
     * El primer resultado de la lista es el calculo de interes mas reciente
     * para esa cuenta, y lo que banco-bff usa como saldo actual en los 3 canales.
     */
    public List<InteresResponse> buscarPorCuenta(Long cuentaId) {
        return jdbcClient.sql("SELECT " + COLUMNAS
                + " FROM intereses_calculados WHERE cuenta_id = :cuentaId ORDER BY id DESC")
                .param("cuentaId", cuentaId)
                .query(this::mapear)
                .list();
    }

    /**
     * Convierte cada fila del ResultSet en un InteresResponse.
     */
    private InteresResponse mapear(ResultSet rs, int rowNum) throws SQLException {
        return new InteresResponse(
                rs.getLong("id"),
                rs.getLong("cuenta_id"),
                rs.getString("nombre"),
                rs.getString("tipo"),
                rs.getBigDecimal("saldo_inicial"),
                rs.getBigDecimal("tasa_aplicada"),
                rs.getBigDecimal("interes_mensual"),
                rs.getBigDecimal("saldo_final"));
    }
}
