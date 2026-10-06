package com.duoc.bancoxyz.servicios.controller;

import com.duoc.bancoxyz.servicios.exception.SolicitudInvalidaException;
import com.duoc.bancoxyz.servicios.logging.CorrelationIdFilter;
import com.duoc.bancoxyz.servicios.retiro.RetiroRequest;
import com.duoc.bancoxyz.servicios.retiro.RetiroResponse;
import com.duoc.bancoxyz.servicios.retiro.RetiroService;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada de la Saga de retiros (Semana 7).
 *
 * POST: el retiro NO se completa dentro de la
 * peticion HTTP, se procesa de forma asincrona a traves de ActiveMQ.
 * El cliente consulta el resultado con GET /api/retiros/{id}.
 *
 * El header Idempotency-Key evita retiros dobles si el cliente reintenta
 * el mismo POST (misma key = mismo retiro, no se crea otro).
 */
@RestController
@RequestMapping("/api/retiros")
public class RetiroController {

    private final RetiroService retiroService;

    public RetiroController(RetiroService retiroService) {
        this.retiroService = retiroService;
    }

    @PostMapping
    public ResponseEntity<RetiroResponse> solicitar(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody RetiroRequest request) {
        if (idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new SolicitudInvalidaException("El header Idempotency-Key debe tener entre 1 y 100 caracteres");
        }
        RetiroService.Registro registro = retiroService.registrar(
                request, idempotencyKey, MDC.get(CorrelationIdFilter.MDC_KEY));

        if (!registro.nueva()) {
            return ResponseEntity.ok(RetiroResponse.de(registro.solicitud(),
                    "Idempotency-Key ya utilizada: se devuelve el retiro existente, no se creo uno nuevo"));
        }
        String mensaje = registro.enviada()
                ? "Retiro recibido, en proceso. Consulte GET /api/retiros/" + registro.solicitud().id()
                : "Retiro registrado. El broker de mensajeria no esta disponible; se enviara automaticamente cuando se recupere";
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(RetiroResponse.de(registro.solicitud(), mensaje));
    }

    @GetMapping("/{id}")
    public RetiroResponse consultar(@PathVariable String id) {
        return RetiroResponse.de(retiroService.obtener(id), null);
    }
}
