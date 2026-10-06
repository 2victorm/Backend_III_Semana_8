package com.duoc.bancoxyz.notificaciones.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Suscriptor 1 del topico: avisa al cliente como termino su retiro.
 */
@Component
public class NotificacionClienteListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionClienteListener.class);

    private final EventosProcesados procesados = new EventosProcesados(10_000);

    @JmsListener(destination = JmsConfig.TOPICO_RETIROS_EVENTOS)
    public void onRetiroFinalizado(RetiroFinalizadoEvent evento) {
        MDC.put("correlationId", evento.correlationId());
        try {
            if (!procesados.registrar(evento.eventId())) {
                log.info("[IDEMPOTENCIA] Notificacion del evento {} ya enviada, se ignora", evento.eventId());
                return;
            }
            String texto = switch (evento.estadoFinal()) {
                case "APROBADA" -> "Retiro de $" + evento.monto() + " realizado con exito.";
                case "RECHAZADA" -> "Tu retiro de $" + evento.monto() + " fue rechazado: " + evento.motivo() + ".";
                case "REVERTIDA" -> "No pudimos entregar tu retiro de $" + evento.monto()
                        + ". El monto fue devuelto a tu cuenta.";
                default -> "Tu retiro termino en estado " + evento.estadoFinal() + ".";
            };
            log.info("[NOTIFICACION] Cuenta {} -> \"{}\" (retiro {})", evento.cuentaId(), texto, evento.retiroId());
        } finally {
            MDC.remove("correlationId");
        }
    }
}
