package com.duoc.bancoxyz.servicios.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Reemplaza la respuesta por defecto de Spring Security (texto plano)
 * cuando falta el JWT o es invalido, para que salga en el mismo
 * formato JSON que el resto de los errores de la API.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().printf(
                "{\"codigo\":401,\"estado\":\"UNAUTHORIZED\","
                        + "\"mensaje\":\"Se requiere un token JWT valido\","
                        + "\"detalles\":[],\"ruta\":\"%s\",\"fechaHora\":\"%s\"}",
                escapar(request.getRequestURI()), OffsetDateTime.now());
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
