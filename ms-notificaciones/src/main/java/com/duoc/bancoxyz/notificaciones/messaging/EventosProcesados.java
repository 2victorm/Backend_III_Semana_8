package com.duoc.bancoxyz.notificaciones.messaging;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Deduplicacion por eventId. Guarda en memoria los ultimos eventId vistos por
 * cada
 * suscriptor.
 *
 * Es memoria local (se pierde al reiniciar).
 * En produccion se usaria una tabla.
 */
public class EventosProcesados {

    private final Set<String> vistos;

    public EventosProcesados(int capacidad) {
        this.vistos = Collections.newSetFromMap(Collections.synchronizedMap(
                new LinkedHashMap<String, Boolean>(capacidad, 0.75f, false) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                        return size() > capacidad;
                    }
                }));
    }

    /** @return true si es la primera vez que se ve este eventId. */
    public boolean registrar(String eventId) {
        return vistos.add(eventId);
    }
}
