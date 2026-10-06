package com.duoc.bancoxyz.servicios.retiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Complemento del fallback de RetiroEventPublisher: cuando ActiveMQ
 * esta caido, las solicitudes quedan en Registrada (guardadas en BD,
 * no perdidas). Cada cierto tiempo se reintenta publicarlas. En cuanto el
 * broker vuelve, la Saga continua sola.
 */
@Component
public class ReenvioPendientesScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReenvioPendientesScheduler.class);

    private final SolicitudRetiroRepository repository;
    private final RetiroService retiroService;
    private final long antiguedadMinimaSegundos;

    public ReenvioPendientesScheduler(SolicitudRetiroRepository repository, RetiroService retiroService,
            @Value("${retiros.reenvio.antiguedad-minima-segundos:10}") long antiguedadMinimaSegundos) {
        this.repository = repository;
        this.retiroService = retiroService;
        this.antiguedadMinimaSegundos = antiguedadMinimaSegundos;
    }

    @Scheduled(fixedDelayString = "${retiros.reenvio.intervalo-ms:15000}", initialDelayString = "${retiros.reenvio.intervalo-ms:15000}")
    public void reenviar() {
        List<SolicitudRetiro> pendientes;
        try {
            pendientes = repository.buscarRegistradasAntesDe(
                    OffsetDateTime.now().minusSeconds(antiguedadMinimaSegundos), 50);
        } catch (RuntimeException e) {
            log.debug("Reenvio omitido, la BD no responde: {}", e.toString());
            return;
        }
        if (pendientes.isEmpty()) {
            return;
        }
        log.info("[RESILIENCIA] {} retiro(s) pendientes de envio al broker, reintentando", pendientes.size());
        for (SolicitudRetiro s : pendientes) {
            if (!retiroService.enviarSolicitud(s)) {
                log.info("[RESILIENCIA] El broker sigue sin responder, se reintentara en el proximo ciclo");
                return;
            }
            log.info("[RESILIENCIA] Retiro {} reenviado correctamente", s.id());
        }
    }
}
