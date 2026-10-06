package com.duoc.bancoxyz.cuentas.messaging;

import com.duoc.bancoxyz.cuentas.messaging.event.ResultadoCuentaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica la respuesta de ms-cuentas. Si falla, la excepcion sube al
 * listener y ActiveMQ reentrega el mensaje original.
 */
@Component
public class ResultadoPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResultadoPublisher.class);

    private final JmsTemplate jmsTemplate;

    public ResultadoPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publicar(ResultadoCuentaEvent resultado) {
        jmsTemplate.convertAndSend(Destinos.COLA_RETIROS_RESULTADO, resultado);
        log.info("[SAGA] -> {} ResultadoCuenta retiro={} resultado={} saldo={}",
                Destinos.COLA_RETIROS_RESULTADO, resultado.retiroId(), resultado.resultado(), resultado.saldoActual());
    }
}
