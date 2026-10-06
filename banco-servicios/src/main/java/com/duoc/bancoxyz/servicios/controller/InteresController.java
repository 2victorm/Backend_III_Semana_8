package com.duoc.bancoxyz.servicios.controller;

import com.duoc.bancoxyz.servicios.model.InteresResponse;
import com.duoc.bancoxyz.servicios.service.InteresService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone /api/intereses.
 * GET sin parametro devuelve todas las cuentas.
 * GET /{cuentaId} filtra por cuenta. Este es el que consume
 * banco-bff para obtener el saldo actual.
 */
@RestController
@RequestMapping("/api/intereses")
public class InteresController {

    private final InteresService service;

    public InteresController(InteresService service) {
        this.service = service;
    }

    @GetMapping
    public List<InteresResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{cuentaId}")
    public List<InteresResponse> buscarPorCuenta(@PathVariable Long cuentaId) {
        return service.buscarPorCuenta(cuentaId);
    }
}
