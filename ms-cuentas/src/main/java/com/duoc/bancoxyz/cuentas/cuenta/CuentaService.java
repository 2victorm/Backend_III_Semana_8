package com.duoc.bancoxyz.cuentas.cuenta;

import com.duoc.bancoxyz.cuentas.messaging.event.CompensarRetiroEvent;
import com.duoc.bancoxyz.cuentas.messaging.event.ResultadoCuentaEvent;
import com.duoc.bancoxyz.cuentas.messaging.event.RetiroSolicitadoEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Transacciones locales de ms-cuentas dentro de la Saga. Cada metodo es
 * una transaccion de BD.
 * 
 * @CircuitBreaker cuentasDb: protege el acceso a PostgreSQL.
 *                 Sin fallback a proposito: si la BD falla, la excepcion llega
 *                 al listener, la sesion JMS hace rollback y el mensaje vuelve
 *                 a la cola.
 */
@Service
public class CuentaService {

    private static final Logger log = LoggerFactory.getLogger(CuentaService.class);

    private final SaldoRepository saldoRepository;
    private final ProcesamientoRepository procesamientoRepository;
    private final String instancia;

    public CuentaService(SaldoRepository saldoRepository, ProcesamientoRepository procesamientoRepository,
            @Value("${spring.application.name}:${server.port}") String instancia) {
        this.saldoRepository = saldoRepository;
        this.procesamientoRepository = procesamientoRepository;
        this.instancia = instancia;
    }

    @Transactional
    @CircuitBreaker(name = "cuentasDb")
    public ResultadoCuentaEvent debitar(RetiroSolicitadoEvent evento) {
        Optional<Procesamiento> previo = procesamientoRepository.buscar(evento.retiroId(), Procesamiento.DEBITO);
        if (previo.isPresent()) {
            log.info("[IDEMPOTENCIA] Retiro {} ya fue procesado por {} ({}), se reenvia el mismo resultado",
                    evento.retiroId(), previo.get().instancia(), previo.get().resultado());
            return aEvento(previo.get(), evento.correlationId());
        }

        Optional<BigDecimal> saldoNuevo = saldoRepository.debitarSiAlcanza(evento.cuentaId(), evento.monto());
        Procesamiento p;
        if (saldoNuevo.isPresent()) {
            p = new Procesamiento(evento.retiroId(), Procesamiento.DEBITO, evento.cuentaId(), evento.monto(),
                    ResultadoCuentaEvent.DEBITADO, null, saldoNuevo.get(), instancia);
            log.info("[SAGA] Cuenta {} DEBITADA en {} -> saldo {}", evento.cuentaId(), evento.monto(),
                    saldoNuevo.get());
        } else {
            Optional<BigDecimal> saldoActual = saldoRepository.buscarSaldo(evento.cuentaId());
            String motivo = saldoActual.isEmpty()
                    ? "La cuenta " + evento.cuentaId() + " no existe"
                    : "Saldo insuficiente: disponible " + saldoActual.get() + ", solicitado " + evento.monto();
            p = new Procesamiento(evento.retiroId(), Procesamiento.DEBITO, evento.cuentaId(), evento.monto(),
                    ResultadoCuentaEvent.RECHAZADO, motivo, saldoActual.orElse(null), instancia);
            log.info("[SAGA] Retiro {} RECHAZADO: {}", evento.retiroId(), motivo);
        }
        procesamientoRepository.insertar(p);
        return aEvento(p, evento.correlationId());
    }

    @Transactional
    @CircuitBreaker(name = "cuentasDb")
    public ResultadoCuentaEvent revertir(CompensarRetiroEvent evento) {
        Optional<Procesamiento> previo = procesamientoRepository.buscar(evento.retiroId(), Procesamiento.REVERSO);
        if (previo.isPresent()) {
            log.info("[IDEMPOTENCIA] Compensacion del retiro {} ya aplicada, se reenvia el resultado",
                    evento.retiroId());
            return aEvento(previo.get(), evento.correlationId());
        }

        boolean huboDebito = procesamientoRepository.buscar(evento.retiroId(), Procesamiento.DEBITO)
                .map(d -> ResultadoCuentaEvent.DEBITADO.equals(d.resultado()))
                .orElse(false);

        Procesamiento p;
        if (huboDebito) {
            BigDecimal saldo = saldoRepository.acreditar(evento.cuentaId(), evento.monto()).orElse(null);
            p = new Procesamiento(evento.retiroId(), Procesamiento.REVERSO, evento.cuentaId(), evento.monto(),
                    ResultadoCuentaEvent.REVERTIDO, evento.motivo(), saldo, instancia);
            log.info("[SAGA] COMPENSACION: cuenta {} reintegrada en {} -> saldo {}",
                    evento.cuentaId(), evento.monto(), saldo);
        } else {
            // Nada que deshacer (no hubo debito): igual se responde REVERTIDO para cerrar
            // la Saga.
            p = new Procesamiento(evento.retiroId(), Procesamiento.REVERSO, evento.cuentaId(), BigDecimal.ZERO,
                    ResultadoCuentaEvent.REVERTIDO, "No existia debito que revertir", null, instancia);
            log.warn("[SAGA] Compensacion del retiro {} sin debito previo", evento.retiroId());
        }
        procesamientoRepository.insertar(p);
        return aEvento(p, evento.correlationId());
    }

    public Optional<BigDecimal> consultarSaldo(Long cuentaId) {
        return saldoRepository.buscarSaldo(cuentaId);
    }

    private static ResultadoCuentaEvent aEvento(Procesamiento p, String correlationId) {
        String eventId = UUID.nameUUIDFromBytes((p.retiroId() + ":" + p.etapa())
                .getBytes(StandardCharsets.UTF_8)).toString();
        return new ResultadoCuentaEvent(eventId, p.retiroId(), p.cuentaId(), p.monto(), p.resultado(),
                p.motivo(), p.saldoResultante(), correlationId, OffsetDateTime.now());
    }
}
