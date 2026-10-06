package com.duoc.bancoxyz.servicios.service;

import com.duoc.bancoxyz.servicios.exception.ServicioNoDisponibleException;
import com.duoc.bancoxyz.servicios.model.TransaccionResponse;
import com.duoc.bancoxyz.servicios.repository.TransaccionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa intermedia entre controller y repository.
 * El acceso a BD queda protegido con Circuit Breaker. Si
 * buscarTodas() falla repetidamente, el circuito se abre y las
 * siguientes llamadas van directo al fallback sin ni siquiera
 * intentar tocar la base de datos.
 */
@Service
public class TransaccionService {

    private final TransaccionRepository repository;

    public TransaccionService(TransaccionRepository repository) {
        this.repository = repository;
    }

    @CircuitBreaker(name = "bancoServiciosDb", fallbackMethod = "listarFallback")
    public List<TransaccionResponse> listar() {
        return repository.buscarTodas();
    }

    // No atrapar la excepcion original dentro de listar(): tiene que
    // propagarse para que el aspecto de Resilience4j la detecte como
    // fallo y cuente hacia el umbral del circuito.
    private List<TransaccionResponse> listarFallback(Throwable t) {
        throw new ServicioNoDisponibleException(
                "No se pudo obtener el listado de transacciones: la base de datos no responde.", t);
    }
}
