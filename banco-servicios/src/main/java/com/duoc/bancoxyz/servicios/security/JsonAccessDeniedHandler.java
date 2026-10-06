package com.duoc.bancoxyz.servicios.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Reemplaza la respuesta por defecto de Spring Security cuando el
 * token es valido pero el rol no alcanza (por ejemplo, un JWT sin
 * rol interno), para que tambien salga en formato JSON.
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().printf(
                "{\"codigo\":403,\"estado\":\"FORBIDDEN\","
                        + "\"mensaje\":\"El token no tiene permisos para acceder a este recurso\","
                        + "\"detalles\":[],\"ruta\":\"%s\",\"fechaHora\":\"%s\"}",
                escapar(request.getRequestURI()), OffsetDateTime.now());
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
