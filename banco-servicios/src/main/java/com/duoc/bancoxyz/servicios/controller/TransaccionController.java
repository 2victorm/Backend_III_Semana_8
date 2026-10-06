package com.duoc.bancoxyz.servicios.controller;

import com.duoc.bancoxyz.servicios.model.TransaccionResponse;
import com.duoc.bancoxyz.servicios.service.TransaccionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone /api/transacciones. Solo un endpoint (sin filtro por cuenta)
 * porque la tabla de origen no tiene cuenta_id.
 */
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService service;

    public TransaccionController(TransaccionService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransaccionResponse> listar() {
        return service.listar();
    }
}
