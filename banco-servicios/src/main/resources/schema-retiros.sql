-- Semana 7: tabla propia de banco-servicios para la Saga de retiros.
-- Se ejecuta en cada arranque (spring.sql.init.mode=always); IF NOT EXISTS
-- la hace idempotente y no toca las tablas migradas por Spring Batch.
CREATE TABLE IF NOT EXISTS solicitudes_retiro (
    id                   VARCHAR(36)   PRIMARY KEY,
    idempotency_key      VARCHAR(100)  NOT NULL UNIQUE,
    cuenta_id            BIGINT        NOT NULL,
    monto                NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    simular_falla_cajero BOOLEAN       NOT NULL DEFAULT FALSE,
    estado               VARCHAR(20)   NOT NULL,
    motivo               VARCHAR(255),
    correlation_id       VARCHAR(64),
    creado_en            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    actualizado_en       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_solicitudes_retiro_estado ON solicitudes_retiro (estado, actualizado_en);
