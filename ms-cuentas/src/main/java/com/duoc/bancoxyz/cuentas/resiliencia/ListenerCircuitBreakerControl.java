package com.duoc.bancoxyz.cuentas.resiliencia;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Integra Resilience4j con la mensajeria:
 *
 * Circuito cuentasDb OPEN -> se detienen los listeners JMS.
 * Circuito HALF_OPEN / CLOSED -> se reanudan.
 *
 */
@Component
public class ListenerCircuitBreakerControl {

    private static final Logger log = LoggerFactory.getLogger(ListenerCircuitBreakerControl.class);
    private static final String CIRCUITO = "cuentasDb";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final JmsListenerEndpointRegistry listenerRegistry;

    public ListenerCircuitBreakerControl(CircuitBreakerRegistry circuitBreakerRegistry,
            JmsListenerEndpointRegistry listenerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.listenerRegistry = listenerRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registrar() {
        CircuitBreaker circuito = circuitBreakerRegistry.circuitBreaker(CIRCUITO);
        circuito.getEventPublisher().onStateTransition(evento -> {
            CircuitBreaker.State nuevo = evento.getStateTransition().getToState();
            switch (nuevo) {
                case OPEN, FORCED_OPEN -> cambiarListeners(false, nuevo);
                case HALF_OPEN, CLOSED -> cambiarListeners(true, nuevo);
                default -> {
                }
            }
        });
        log.info("[RESILIENCIA] Listeners JMS vinculados al Circuit Breaker '{}'", CIRCUITO);
    }

    private void cambiarListeners(boolean encender, CircuitBreaker.State estado) {
        // En otro hilo: la transicion ocurre dentro de un hilo del propio listener,
        // y un contenedor no deberia detenerse desde su hilo de consumo.
        CompletableFuture.runAsync(() -> {
            for (MessageListenerContainer contenedor : listenerRegistry.getListenerContainers()) {
                if (encender && !contenedor.isRunning()) {
                    contenedor.start();
                } else if (!encender && contenedor.isRunning()) {
                    contenedor.stop();
                }
            }
            if (encender) {
                log.info("[RESILIENCIA] Circuito {} en {} -> consumo de mensajes REANUDADO", CIRCUITO, estado);
            } else {
                log.warn(
                        "[RESILIENCIA] Circuito {} en {} -> consumo de mensajes PAUSADO (los mensajes esperan en ActiveMQ)",
                        CIRCUITO, estado);
            }
        });
    }
}
