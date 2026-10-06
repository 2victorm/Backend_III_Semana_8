package com.duoc.bancoxyz.servicios.exception;

import com.duoc.bancoxyz.servicios.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Centraliza el manejo de excepciones de toda la AP. En vez de que
 * cada controller decida su propio formato de error, todos pasan por
 * aca y salen con la misma forma (ErrorResponse) y el codigo HTTP que
 * corresponda.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> manejarValidacion(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                List<String> detalles = exception.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .toList();
                return construir(HttpStatus.BAD_REQUEST, "Los datos enviados no son validos", detalles, request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> manejarJsonInvalido(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {
                return construir(HttpStatus.BAD_REQUEST,
                                "El cuerpo de la solicitud no contiene un JSON valido", List.of(), request);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> manejarCredenciales(
                        BadCredentialsException exception,
                        HttpServletRequest request) {
                return construir(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos", List.of(), request);
        }

        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<ErrorResponse> manejarHeaderFaltante(
                        MissingRequestHeaderException exception,
                        HttpServletRequest request) {
                return construir(HttpStatus.BAD_REQUEST,
                                "Falta el header obligatorio " + exception.getHeaderName(), List.of(), request);
        }

        @ExceptionHandler(SolicitudInvalidaException.class)
        public ResponseEntity<ErrorResponse> manejarSolicitudInvalida(
                        SolicitudInvalidaException exception,
                        HttpServletRequest request) {
                return construir(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of(), request);
        }

        @ExceptionHandler(RecursoNoEncontradoException.class)
        public ResponseEntity<ErrorResponse> manejarNoEncontrado(
                        RecursoNoEncontradoException exception,
                        HttpServletRequest request) {
                return construir(HttpStatus.NOT_FOUND, exception.getMessage(), List.of(), request);
        }

        /**
         * Semana 7
         * El repositorio de retiros tiene el Circuit Breaker a nivel de
         * clase.
         */
        @ExceptionHandler({ CallNotPermittedException.class, DataAccessException.class })
        public ResponseEntity<ErrorResponse> manejarBdNoDisponible(
                        Exception exception,
                        HttpServletRequest request) {
                log.warn("BD no disponible en {}: {}", request.getRequestURI(), exception.toString());
                return construir(HttpStatus.SERVICE_UNAVAILABLE,
                                "La base de datos no responde, intente nuevamente en unos segundos",
                                List.of("La solicitud fue controlada por el Circuit Breaker bancoServiciosDb"),
                                request);
        }

        @ExceptionHandler(ServicioNoDisponibleException.class)
        public ResponseEntity<ErrorResponse> manejarServicioNoDisponible(
                        ServicioNoDisponibleException exception,
                        HttpServletRequest request) {
                log.warn("Dependencia no disponible en {}: {}", request.getRequestURI(), exception.getMessage());
                return construir(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(),
                                List.of("La solicitud fue controlada por el Circuit Breaker bancoServiciosDb"),
                                request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> manejarErrorGeneral(
                        Exception exception,
                        HttpServletRequest request) {
                log.error("Error no controlado en {}", request.getRequestURI(), exception);
                return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocurrio un error interno al procesar la solicitud", List.of(), request);
        }

        private ResponseEntity<ErrorResponse> construir(
                        HttpStatus estado,
                        String mensaje,
                        List<String> detalles,
                        HttpServletRequest request) {
                ErrorResponse cuerpo = new ErrorResponse(
                                estado.value(),
                                estado.name(),
                                mensaje,
                                detalles,
                                request.getRequestURI(),
                                OffsetDateTime.now());
                return ResponseEntity.status(estado).body(cuerpo);
        }
}
