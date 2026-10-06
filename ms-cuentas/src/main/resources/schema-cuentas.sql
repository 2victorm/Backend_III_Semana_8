-- Tablas propias de ms-cuentas (Semana 7). 
-- Idempotente: se puede correr en cada arranque.

-- Saldo vigente por cuenta.
CREATE TABLE IF NOT EXISTS saldos_cuenta (
    cuenta_id      BIGINT        PRIMARY KEY,
    saldo          NUMERIC(14,2) NOT NULL CHECK (saldo >= 0),
    actualizado_en TIMESTAMPTZ   NOT NULL DEFAULT now()
);

INSERT INTO saldos_cuenta (cuenta_id, saldo)
SELECT DISTINCT ON (cuenta_id) cuenta_id, ROUND(GREATEST(saldo_final, 0), 2)
FROM intereses_calculados
ORDER BY cuenta_id, id DESC
ON CONFLICT (cuenta_id) DO NOTHING;

-- Registro de lo que ms-cuentas ya hizo por cada retiro.
CREATE TABLE IF NOT EXISTS procesamientos_retiro (
    retiro_id        VARCHAR(36)   NOT NULL,
    etapa            VARCHAR(10)   NOT NULL,
    cuenta_id        BIGINT        NOT NULL,
    monto            NUMERIC(14,2) NOT NULL,
    resultado        VARCHAR(12)   NOT NULL,
    motivo           VARCHAR(255),
    saldo_resultante NUMERIC(14,2),
    instancia        VARCHAR(40),
    procesado_en     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    PRIMARY KEY (retiro_id, etapa)
);
