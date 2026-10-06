package com.duoc.bancoxyz.notificaciones.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Suscriptor 2 del mismo topico. Demuestra publicacion-suscripcion.
 * Un solo mensaje publicado por banco-servicios llega a todos los suscriptores.
 * banco-servicios no sabe que existe la auditoria.
 */
@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);

    private final EventosProcesados procesados = new EventosProcesados(10_000);

    @JmsListener(destination = JmsConfig.TOPICO_RETIROS_EVENTOS)
    public void onRetiroFinalizado(RetiroFinalizadoEvent evento) {
        MDC.put("correlationId", evento.correlationId());
        try {
            if (!procesados.registrar(evento.eventId())) {
                return;
            }
            log.info("[AUDITORIA] retiro={} cuenta={} monto={} estado={} motivo={} fecha={}",
                    evento.retiroId(), evento.cuentaId(), evento.monto(), evento.estadoFinal(),
                    evento.motivo() == null ? "-" : evento.motivo(), evento.fecha());
        } finally {
            MDC.remove("correlationId");
        }
    }
}
