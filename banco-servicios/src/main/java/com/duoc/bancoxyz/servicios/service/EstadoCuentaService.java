package com.duoc.bancoxyz.servicios.service;

import com.duoc.bancoxyz.servicios.exception.ServicioNoDisponibleException;
import com.duoc.bancoxyz.servicios.model.EstadoCuentaResponse;
import com.duoc.bancoxyz.servicios.model.MovimientoResponse;
import com.duoc.bancoxyz.servicios.repository.EstadoCuentaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa intermedia entre controller y repository.
 * Comparte el circuito "bancoServiciosDb" con los otros dos
 * servicios.
 */
@Service
public class EstadoCuentaService {

    private final EstadoCuentaRepository repository;

    public EstadoCuentaService(EstadoCuentaRepository repository) {
        this.repository = repository;
    }

    @CircuitBreaker(name = "bancoServiciosDb", fallbackMethod = "listarFallback")
    public List<EstadoCuentaResponse> listar() {
        return repository.buscarTodos();
    }

    @CircuitBreaker(name = "bancoServiciosDb", fallbackMethod = "buscarPorCuentaFallback")
    public List<EstadoCuentaResponse> buscarPorCuenta(Long cuentaId) {
        return repository.buscarPorCuenta(cuentaId);
    }

    @CircuitBreaker(name = "bancoServiciosDb", fallbackMethod = "buscarMovimientosFallback")
    public List<MovimientoResponse> buscarMovimientos(Long cuentaId) {
        return repository.buscarMovimientosPorCuenta(cuentaId);
    }

    private List<EstadoCuentaResponse> listarFallback(Throwable t) {
        throw new ServicioNoDisponibleException(
                "No se pudo obtener el listado de estados de cuenta: la base de datos no responde.", t);
    }

    private List<EstadoCuentaResponse> buscarPorCuentaFallback(Long cuentaId, Throwable t) {
        throw new ServicioNoDisponibleException(
                "No se pudo obtener el estado de cuenta " + cuentaId + ": la base de datos no responde.", t);
    }

    private List<MovimientoResponse> buscarMovimientosFallback(Long cuentaId, Throwable t) {
        throw new ServicioNoDisponibleException(
                "No se pudieron obtener los movimientos de la cuenta " + cuentaId + ": la base de datos no responde.",
                t);
    }
}
