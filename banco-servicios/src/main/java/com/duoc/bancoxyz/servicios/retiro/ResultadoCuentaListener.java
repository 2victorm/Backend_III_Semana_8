package com.duoc.bancoxyz.servicios.retiro;

import com.duoc.bancoxyz.servicios.logging.CorrelationIdFilter;
import com.duoc.bancoxyz.servicios.messaging.Destinos;
import com.duoc.bancoxyz.servicios.messaging.event.ResultadoCuentaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de la cola banco.retiros.resultado (respuestas de ms-cuentas).
 * Si el procesamiento lanza una excepcion, la sesion JMS (transaccional)
 * hace rollback y ActiveMQ reentrega el mensaje. Tras agotar los
 * reintentos lo mueve a la cola ActiveMQ.DLQ.
 */
@Component
public class ResultadoCuentaListener {

    private static final Logger log = LoggerFactory.getLogger(ResultadoCuentaListener.class);

    private final RetiroService retiroService;

    public ResultadoCuentaListener(RetiroService retiroService) {
        this.retiroService = retiroService;
    }

    @JmsListener(destination = Destinos.COLA_RETIROS_RESULTADO)
    public void recibir(ResultadoCuentaEvent evento) {
        MDC.put(CorrelationIdFilter.MDC_KEY, evento.correlationId());
        try {
            log.info("[SAGA] <- {} ResultadoCuenta retiro={} resultado={} eventId={}",
                    Destinos.COLA_RETIROS_RESULTADO, evento.retiroId(), evento.resultado(), evento.eventId());
            retiroService.procesarResultado(evento);
        } finally {
            MDC.remove(CorrelationIdFilter.MDC_KEY);
        }
    }
}
