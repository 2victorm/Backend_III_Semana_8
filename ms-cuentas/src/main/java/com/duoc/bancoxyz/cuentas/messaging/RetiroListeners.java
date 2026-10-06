package com.duoc.bancoxyz.cuentas.messaging;

import com.duoc.bancoxyz.cuentas.cuenta.CuentaService;
import com.duoc.bancoxyz.cuentas.messaging.event.CompensarRetiroEvent;
import com.duoc.bancoxyz.cuentas.messaging.event.RetiroSolicitadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Consumidores de ms-cuentas.
 *
 * Las colas JMS reparten cada mensaje a un solo consumidor.
 * Por eso se puede escalar:
 * - Vertical: concurrency "2-4" -> entre 2 y 4 hilos consumiendo a la vez.
 * - Horizontal: levantar otra instancia de ms-cuentas (otro puerto). ActiveMQ
 * reparte los mensajes entre todas las instancias conectadas.
 * El log muestra en que instancia y en que hilo se proceso cada retiro.
 */
@Component
public class RetiroListeners {

    private static final Logger log = LoggerFactory.getLogger(RetiroListeners.class);
    private static final String MDC_KEY = "correlationId";

    public static final String ID_SOLICITUDES = "listenerSolicitudes";
    public static final String ID_COMPENSACIONES = "listenerCompensaciones";

    private final CuentaService cuentaService;
    private final ResultadoPublisher publisher;

    public RetiroListeners(CuentaService cuentaService, ResultadoPublisher publisher) {
        this.cuentaService = cuentaService;
        this.publisher = publisher;
    }

    @JmsListener(id = ID_SOLICITUDES, destination = Destinos.COLA_RETIROS_SOLICITADOS, concurrency = "${cuentas.listener.concurrencia:2-4}")
    public void onRetiroSolicitado(RetiroSolicitadoEvent evento) {
        MDC.put(MDC_KEY, evento.correlationId());
        try {
            log.info("[SAGA] <- {} RetiroSolicitado retiro={} cuenta={} monto={}",
                    Destinos.COLA_RETIROS_SOLICITADOS, evento.retiroId(), evento.cuentaId(), evento.monto());
            publisher.publicar(cuentaService.debitar(evento));
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    @JmsListener(id = ID_COMPENSACIONES, destination = Destinos.COLA_RETIROS_COMPENSACION, concurrency = "${cuentas.listener.concurrencia:2-4}")
    public void onCompensarRetiro(CompensarRetiroEvent evento) {
        MDC.put(MDC_KEY, evento.correlationId());
        try {
            log.info("[SAGA] <- {} CompensarRetiro retiro={} motivo='{}'",
                    Destinos.COLA_RETIROS_COMPENSACION, evento.retiroId(), evento.motivo());
            publisher.publicar(cuentaService.revertir(evento));
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
