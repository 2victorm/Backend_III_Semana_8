package com.duoc.bancoxyz.servicios.retiro;

import com.duoc.bancoxyz.servicios.exception.RecursoNoEncontradoException;
import com.duoc.bancoxyz.servicios.messaging.RetiroEventPublisher;
import com.duoc.bancoxyz.servicios.messaging.event.CompensarRetiroEvent;
import com.duoc.bancoxyz.servicios.messaging.event.ResultadoCuentaEvent;
import com.duoc.bancoxyz.servicios.messaging.event.RetiroFinalizadoEvent;
import com.duoc.bancoxyz.servicios.messaging.event.RetiroSolicitadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Participante "banco-servicios" de la Saga de retiros (coreografia).
 * La lógica de la Saga (registrar, simular el cajero, compensar y cerrar).
 */
@Service
public class RetiroService {

    private static final Logger log = LoggerFactory.getLogger(RetiroService.class);

    private static final Set<EstadoRetiro> ESPERANDO_CUENTAS = EnumSet.of(EstadoRetiro.REGISTRADA,
            EstadoRetiro.ENVIADA);

    private final SolicitudRetiroRepository repository;
    private final RetiroEventPublisher publisher;

    public RetiroService(SolicitudRetiroRepository repository, RetiroEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    /**
     * Resultado del POST: la solicitud, si es nueva y si alcanzo a llegar al
     * broker.
     */
    public record Registro(SolicitudRetiro solicitud, boolean nueva, boolean enviada) {
    }

    // ------------------------------------------------------------------
    // Paso 1: registrar y publicar
    // ------------------------------------------------------------------

    public Registro registrar(RetiroRequest request, String idempotencyKey, String correlationId) {
        SolicitudRetiro nueva = new SolicitudRetiro(
                UUID.randomUUID().toString(), idempotencyKey, request.cuentaId(), request.monto(),
                request.fallaCajero(), EstadoRetiro.REGISTRADA, null, correlationId, null, null);

        if (repository.insertarSiNoExiste(nueva) == 0) {
            SolicitudRetiro existente = repository.buscarPorIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency-Key en conflicto sin fila asociada"));
            log.info("[IDEMPOTENCIA] Idempotency-Key {} ya usada, se devuelve el retiro {} sin crear otro",
                    idempotencyKey, existente.id());
            return new Registro(existente, false, existente.estado() != EstadoRetiro.REGISTRADA);
        }

        log.info("[SAGA] Retiro {} REGISTRADO cuenta={} monto={}", nueva.id(), nueva.cuentaId(), nueva.monto());
        boolean enviada = enviarSolicitud(nueva);
        return new Registro(obtener(nueva.id()), true, enviada);
    }

    /** Usado por el POST y por ReenvioPendientesScheduler. */
    boolean enviarSolicitud(SolicitudRetiro s) {
        RetiroSolicitadoEvent evento = new RetiroSolicitadoEvent(
                eventId(s.id(), "SOLICITADO"), s.id(), s.cuentaId(), s.monto(), s.correlationId(),
                OffsetDateTime.now());
        boolean enviada = publisher.publicarSolicitud(evento);
        if (enviada) {
            repository.cambiarEstado(s.id(), EnumSet.of(EstadoRetiro.REGISTRADA), EstadoRetiro.ENVIADA, null);
        }
        return enviada;
    }

    public SolicitudRetiro obtener(String retiroId) {
        return repository.buscarPorId(retiroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el retiro " + retiroId));
    }

    // ------------------------------------------------------------------
    // Pasos siguientes: reaccionar a ms-cuentas
    // ------------------------------------------------------------------

    public void procesarResultado(ResultadoCuentaEvent evento) {
        SolicitudRetiro solicitud = repository.buscarPorId(evento.retiroId()).orElse(null);
        if (solicitud == null) {
            log.warn("[SAGA] ResultadoCuenta para retiro desconocido {}, se descarta", evento.retiroId());
            return;
        }

        switch (evento.resultado()) {
            case ResultadoCuentaEvent.RECHAZADO -> transicionarYFinalizar(solicitud, ESPERANDO_CUENTAS,
                    EstadoRetiro.RECHAZADA, evento.motivo());
            case ResultadoCuentaEvent.DEBITADO -> entregarEfectivo(solicitud, evento);
            case ResultadoCuentaEvent.REVERTIDO -> transicionarYFinalizar(solicitud,
                    EnumSet.of(EstadoRetiro.EN_COMPENSACION), EstadoRetiro.REVERTIDA,
                    "Saldo reintegrado tras falla del cajero");
            default ->
                log.warn("[SAGA] Resultado desconocido '{}' para retiro {}", evento.resultado(), evento.retiroId());
        }
    }

    /** Simula el dispensador del cajero una vez que el saldo ya fue debitado. */
    private void entregarEfectivo(SolicitudRetiro s, ResultadoCuentaEvent evento) {
        if (!s.simularFallaCajero()) {
            log.info("[CAJERO] Entregando ${} de la cuenta {} (saldo restante {})",
                    s.monto(), s.cuentaId(), evento.saldoActual());
            transicionarYFinalizar(s, ESPERANDO_CUENTAS, EstadoRetiro.APROBADA, null);
            return;
        }

        String motivo = "Falla del dispensador del cajero: efectivo no entregado";
        log.warn("[CAJERO] {} (retiro {}). Se inicia la COMPENSACION", motivo, s.id());
        boolean cambio = repository.cambiarEstado(s.id(), ESPERANDO_CUENTAS, EstadoRetiro.EN_COMPENSACION, motivo);
        if (!cambio && !estadoActualEs(s.id(), EstadoRetiro.EN_COMPENSACION)) {
            log.info("[IDEMPOTENCIA] Retiro {} ya no esperaba el debito, mensaje duplicado ignorado", s.id());
            return;
        }
        // Si cambio == false pero ya esta EN_COMPENSACION, es una reentrega: se vuelve
        // a
        // publicar la compensacion (ms-cuentas la ignora si ya la aplico).
        CompensarRetiroEvent compensar = new CompensarRetiroEvent(
                eventId(s.id(), "COMPENSAR"), s.id(), s.cuentaId(), s.monto(), motivo, s.correlationId(),
                OffsetDateTime.now());
        exigirPublicado(publisher.publicarCompensacion(compensar), s.id());
    }

    private void transicionarYFinalizar(SolicitudRetiro s, Set<EstadoRetiro> desde, EstadoRetiro hacia, String motivo) {
        boolean cambio = repository.cambiarEstado(s.id(), desde, hacia, motivo);
        if (!cambio && !estadoActualEs(s.id(), hacia)) {
            log.info("[IDEMPOTENCIA] Retiro {} esta en otro estado, mensaje duplicado ignorado", s.id());
            return;
        }
        log.info("[SAGA] Retiro {} -> {}{}", s.id(), hacia, motivo != null ? " (" + motivo + ")" : "");
        RetiroFinalizadoEvent finalizado = new RetiroFinalizadoEvent(
                eventId(s.id(), hacia.name()), s.id(), s.cuentaId(), s.monto(), hacia.name(),
                motivo, s.correlationId(), OffsetDateTime.now());
        exigirPublicado(publisher.publicarFinalizado(finalizado), s.id());
    }

    private boolean estadoActualEs(String retiroId, EstadoRetiro estado) {
        return repository.buscarPorId(retiroId).map(actual -> actual.estado() == estado).orElse(false);
    }

    /**
     * Dentro de un listener, si no se pudo publicar el siguiente paso se lanza la
     * excepcion: la sesion JMS hace rollback y ActiveMQ reentrega el mensaje.
     */
    private void exigirPublicado(boolean publicado, String retiroId) {
        if (!publicado) {
            throw new IllegalStateException("No se pudo publicar el siguiente paso de la Saga del retiro " + retiroId);
        }
    }

    /**
     * eventId deterministico (mismo retiro + mismo paso = mismo id): si un paso se
     * publica dos veces, el consumidor ve el mismo eventId y puede descartarlo.
     */
    private static String eventId(String retiroId, String paso) {
        return UUID.nameUUIDFromBytes((retiroId + ":" + paso).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
