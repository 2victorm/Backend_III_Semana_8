package com.duoc.bancoxyz.servicios.messaging;

import com.duoc.bancoxyz.servicios.messaging.event.CompensarRetiroEvent;
import com.duoc.bancoxyz.servicios.messaging.event.RetiroFinalizadoEvent;
import com.duoc.bancoxyz.servicios.messaging.event.RetiroSolicitadoEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Unico punto de salida de mensajes de banco-servicios hacia ActiveMQ.
 *
 * Publica los eventos con @Retry + @CircuitBreaker("brokerJms") y un fallback.
 */
@Component
public class RetiroEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventPublisher.class);
    private static final String BROKER = "brokerJms";

    private final JmsTemplate colaTemplate;
    private final JmsTemplate topicoTemplate;

    public RetiroEventPublisher(JmsTemplate jmsTemplate,
            @Qualifier("jmsTopicTemplate") JmsTemplate jmsTopicTemplate) {
        this.colaTemplate = jmsTemplate;
        this.topicoTemplate = jmsTopicTemplate;
    }

    @Retry(name = BROKER, fallbackMethod = "solicitudFallback")
    @CircuitBreaker(name = BROKER)
    public boolean publicarSolicitud(RetiroSolicitadoEvent evento) {
        colaTemplate.convertAndSend(Destinos.COLA_RETIROS_SOLICITADOS, evento);
        log.info("[SAGA] -> {} RetiroSolicitado retiro={} cuenta={} monto={}",
                Destinos.COLA_RETIROS_SOLICITADOS, evento.retiroId(), evento.cuentaId(), evento.monto());
        return true;
    }

    @Retry(name = BROKER, fallbackMethod = "compensacionFallback")
    @CircuitBreaker(name = BROKER)
    public boolean publicarCompensacion(CompensarRetiroEvent evento) {
        colaTemplate.convertAndSend(Destinos.COLA_RETIROS_COMPENSACION, evento);
        log.info("[SAGA] -> {} CompensarRetiro retiro={} motivo='{}'",
                Destinos.COLA_RETIROS_COMPENSACION, evento.retiroId(), evento.motivo());
        return true;
    }

    @Retry(name = BROKER, fallbackMethod = "finalizadoFallback")
    @CircuitBreaker(name = BROKER)
    public boolean publicarFinalizado(RetiroFinalizadoEvent evento) {
        topicoTemplate.convertAndSend(Destinos.TOPICO_RETIROS_EVENTOS, evento);
        log.info("[SAGA] -> {} (topico) RetiroFinalizado retiro={} estado={}",
                Destinos.TOPICO_RETIROS_EVENTOS, evento.retiroId(), evento.estadoFinal());
        return true;
    }

    // Fallbacks: misma firma + Throwable, mismo tipo de retorno.

    private boolean solicitudFallback(RetiroSolicitadoEvent evento, Throwable t) {
        log.warn("[RESILIENCIA] Broker no disponible, RetiroSolicitado {} queda pendiente de reenvio: {}",
                evento.retiroId(), t.toString());
        return false;
    }

    private boolean compensacionFallback(CompensarRetiroEvent evento, Throwable t) {
        log.warn("[RESILIENCIA] No se pudo publicar CompensarRetiro {}: {}", evento.retiroId(), t.toString());
        return false;
    }

    private boolean finalizadoFallback(RetiroFinalizadoEvent evento, Throwable t) {
        log.warn("[RESILIENCIA] No se pudo publicar RetiroFinalizado {}: {}", evento.retiroId(), t.toString());
        return false;
    }
}
