package com.duoc.bancoxyz.servicios.service;

import com.duoc.bancoxyz.servicios.exception.ServicioNoDisponibleException;
import com.duoc.bancoxyz.servicios.model.InteresResponse;
import com.duoc.bancoxyz.servicios.repository.InteresRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa intermedia entre controller y repository.
 * Comparte el circuito "bancoServiciosDb" con los otros dos
 * servicios.
 */
@Service
public class InteresService {

    private final InteresRepository repository;

    public InteresService(InteresRepository repository) {
        this.repository = repository;
    }

    @CircuitBreaker(name = "bancoServiciosDb", fallbackMethod = "listarFallback")
    public List<InteresResponse> listar() {
        return repository.buscarTodos();
    }

    @CircuitBreaker(name = "bancoServiciosDb", fallbackMethod = "buscarPorCuentaFallback")
    public List<InteresResponse> buscarPorCuenta(Long cuentaId) {
        return repository.buscarPorCuenta(cuentaId);
    }

    private List<InteresResponse> listarFallback(Throwable t) {
        throw new ServicioNoDisponibleException(
                "No se pudo obtener el listado de intereses: la base de datos no responde.", t);
    }

    // El fallback debe repetir los mismos parametros de negocio del
    // metodo original, agregando el Throwable al final.
    private List<InteresResponse> buscarPorCuentaFallback(Long cuentaId, Throwable t) {
        throw new ServicioNoDisponibleException(
                "No se pudo obtener el interes de la cuenta " + cuentaId + ": la base de datos no responde.", t);
    }
}
