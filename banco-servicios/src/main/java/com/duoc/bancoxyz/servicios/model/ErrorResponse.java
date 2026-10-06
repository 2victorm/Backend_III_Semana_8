package com.duoc.bancoxyz.servicios.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cuerpo estandar para cualquier respuesta de error de la API.
 * Codigo HTTP, nombre del estado, mensaje legible, detalles
 * adicionales (por ejemplo, uno por cada campo invalido) y cuando/
 * donde ocurrio.
 */
public record ErrorResponse(
                int codigo,
                String estado,
                String mensaje,
                List<String> detalles,
                String ruta,
                OffsetDateTime fechaHora) {
}
