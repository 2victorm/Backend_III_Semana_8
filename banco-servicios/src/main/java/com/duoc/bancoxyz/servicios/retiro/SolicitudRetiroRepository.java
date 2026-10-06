package com.duoc.bancoxyz.servicios.retiro;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a solicitudes_retiro. Todo el acceso a BD de este repositorio
 * pasa por el mismo Circuit Breaker de la Semana 6 (bancoServiciosDb),
 * anotado a nivel de clase.
 */
@Repository
@CircuitBreaker(name = "bancoServiciosDb")
public class SolicitudRetiroRepository {

    private static final String COLUMNAS = "id, idempotency_key, cuenta_id, monto, simular_falla_cajero, "
            + "estado, motivo, correlation_id, creado_en, actualizado_en";

    private final JdbcClient jdbcClient;

    public SolicitudRetiroRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * @return 1 si se inserto, 0 si ya existia una solicitud con esa
     *         Idempotency-Key.
     */
    public int insertarSiNoExiste(SolicitudRetiro s) {
        return jdbcClient.sql("""
                INSERT INTO solicitudes_retiro
                    (id, idempotency_key, cuenta_id, monto, simular_falla_cajero, estado, correlation_id)
                VALUES (:id, :key, :cuentaId, :monto, :falla, :estado, :correlationId)
                ON CONFLICT (idempotency_key) DO NOTHING
                """)
                .param("id", s.id())
                .param("key", s.idempotencyKey())
                .param("cuentaId", s.cuentaId())
                .param("monto", s.monto())
                .param("falla", s.simularFallaCajero())
                .param("estado", s.estado().name())
                .param("correlationId", s.correlationId())
                .update();
    }

    public Optional<SolicitudRetiro> buscarPorId(String id) {
        return jdbcClient.sql("SELECT " + COLUMNAS + " FROM solicitudes_retiro WHERE id = :id")
                .param("id", id)
                .query(this::mapear)
                .optional();
    }

    public Optional<SolicitudRetiro> buscarPorIdempotencyKey(String key) {
        return jdbcClient.sql("SELECT " + COLUMNAS + " FROM solicitudes_retiro WHERE idempotency_key = :key")
                .param("key", key)
                .query(this::mapear)
                .optional();
    }

    /**
     * Solicitudes que no alcanzaron a llegar al broker y llevan un rato esperando.
     */
    public List<SolicitudRetiro> buscarRegistradasAntesDe(OffsetDateTime limite, int maximo) {
        return jdbcClient.sql("SELECT " + COLUMNAS + " FROM solicitudes_retiro "
                + "WHERE estado = 'REGISTRADA' AND actualizado_en < :limite ORDER BY creado_en LIMIT :maximo")
                .param("limite", limite)
                .param("maximo", maximo)
                .query(this::mapear)
                .list();
    }

    /**
     * Cambia el estado solo si el actual es uno de los esperados.
     * 
     * @return true si la fila cambio; false si el estado ya no era el esperado
     *         (duplicado).
     */
    public boolean cambiarEstado(String id, Collection<EstadoRetiro> desde, EstadoRetiro hacia, String motivo) {
        List<String> estadosOrigen = desde.stream().map(Enum::name).toList();
        String setMotivo = motivo != null ? ", motivo = :motivo" : "";
        JdbcClient.StatementSpec spec = jdbcClient.sql("UPDATE solicitudes_retiro SET estado = :hacia"
                + setMotivo + ", actualizado_en = now() WHERE id = :id AND estado IN (:desde)")
                .param("hacia", hacia.name())
                .param("id", id)
                .param("desde", estadosOrigen);
        if (motivo != null) {
            spec = spec.param("motivo", motivo);
        }
        return spec.update() == 1;
    }

    private SolicitudRetiro mapear(ResultSet rs, int rowNum) throws SQLException {
        return new SolicitudRetiro(
                rs.getString("id"),
                rs.getString("idempotency_key"),
                rs.getLong("cuenta_id"),
                rs.getBigDecimal("monto"),
                rs.getBoolean("simular_falla_cajero"),
                EstadoRetiro.valueOf(rs.getString("estado")),
                rs.getString("motivo"),
                rs.getString("correlation_id"),
                rs.getObject("creado_en", OffsetDateTime.class),
                rs.getObject("actualizado_en", OffsetDateTime.class));
    }
}
