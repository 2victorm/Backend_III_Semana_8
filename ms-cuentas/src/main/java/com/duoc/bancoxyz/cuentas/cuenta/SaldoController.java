package com.duoc.bancoxyz.cuentas.cuenta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Consulta de apoyo para las evidencias: permite ver el saldo antes y
 * despues de un retiro (y despues de una compensacion).
 */
@RestController
@RequestMapping("/api/cuentas")
public class SaldoController {

    public record SaldoResponse(Long cuentaId, BigDecimal saldo) {
    }

    private final CuentaService cuentaService;

    public SaldoController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/{cuentaId}/saldo")
    public ResponseEntity<SaldoResponse> saldo(@PathVariable Long cuentaId) {
        return cuentaService.consultarSaldo(cuentaId)
                .map(saldo -> ResponseEntity.ok(new SaldoResponse(cuentaId, saldo)))
                .orElse(ResponseEntity.notFound().build());
    }
}
