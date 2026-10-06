package com.duoc.bancoxyz.servicios.controller;

import com.duoc.bancoxyz.servicios.model.EstadoCuentaResponse;
import com.duoc.bancoxyz.servicios.model.MovimientoResponse;
import com.duoc.bancoxyz.servicios.service.EstadoCuentaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone /api/estados-anuales, con 3 niveles de detalle. Todos los
 * resumenes, resumenes de una cuenta, y el detalle de movimientos de
 * una cuenta.
 */
@RestController
@RequestMapping("/api/estados-anuales")
public class EstadoCuentaController {

    private final EstadoCuentaService service;

    public EstadoCuentaController(EstadoCuentaService service) {
        this.service = service;
    }

    @GetMapping
    public List<EstadoCuentaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{cuentaId}")
    public List<EstadoCuentaResponse> buscarPorCuenta(@PathVariable Long cuentaId) {
        return service.buscarPorCuenta(cuentaId);
    }

    @GetMapping("/{cuentaId}/movimientos")
    public List<MovimientoResponse> buscarMovimientos(@PathVariable Long cuentaId) {
        return service.buscarMovimientos(cuentaId);
    }
}
