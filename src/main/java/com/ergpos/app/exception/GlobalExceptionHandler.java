package com.ergpos.app.exception;

import com.ergpos.app.dto.auth.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        //Manejo centralizado de todas las BusinessException
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
                logger.warn("BusinessException: {} - {}", ex.getCode(), ex.getMessage());
                ErrorResponseDTO error = new ErrorResponseDTO(
                                ex.getCode(),
                                ex.getMessage(),
                                ex.getStatusCode());
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        //Manejo específico para ResourceNotFoundException
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
                logger.warn("Resource not found: {}", ex.getMessage());
                ErrorResponseDTO error = new ErrorResponseDTO(
                                ex.getCode(),
                                ex.getMessage(),
                                ex.getStatusCode());
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        //Manejo específico para AccountDisabledException
        @ExceptionHandler(AccountDisabledException.class)
        public ResponseEntity<ErrorResponseDTO> handleAccountDisabledException(AccountDisabledException ex) {
                logger.warn("Account disabled: {}", ex.getMessage());
                ErrorResponseDTO error = new ErrorResponseDTO(
                                ex.getCode(),
                                ex.getMessage(),
                                ex.getStatusCode());
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        // Manejo específico para StockInsufficiencyException
        @ExceptionHandler(StockInsufficiencyException.class)
        public ResponseEntity<ErrorResponseDTO> handleStockInsufficiencyException(StockInsufficiencyException ex) {
                logger.warn("Stock insufficient: {}", ex.getMessage());
                ErrorResponseDTO error = new ErrorResponseDTO(
                                ex.getCode(),
                                ex.getMessage(),
                                ex.getStatusCode());
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        // Manejo específico para UnauthorizedException
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex) {
                logger.warn("Unauthorized: {}", ex.getMessage());
                ErrorResponseDTO error = new ErrorResponseDTO(
                                ex.getCode(),
                                ex.getMessage(),
                                ex.getStatusCode());
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        // Manejo de errores de validación (@Valid)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {

                logger.warn("Validation error: {}", ex.getMessage());

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                Map<String, Object> response = new HashMap<>();
                response.put("code", "VALIDATION_ERROR");
                response.put("message", "Errores de validación en los campos enviados");
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("timestamp", LocalDateTime.now());
                response.put("errors", errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        // Manejo de ResponseStatusException (para compatibilidad con código existente)
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(
                        ResponseStatusException ex) {

                logger.warn("ResponseStatusException: {}", ex.getReason());

                // Convertir ResponseStatusException a BusinessException response
                ErrorResponseDTO error = new ErrorResponseDTO(
                                "BUSINESS_ERROR",
                                ex.getReason() != null ? ex.getReason() : "Error en la operación",
                                ex.getStatusCode().value());

                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        // Manejo de errores de autenticación
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
                        AuthenticationException ex) {

                logger.warn("Authentication error: {}", ex.getMessage());

                String message = "Credenciales inválidas";
                String code = "AUTHENTICATION_ERROR";
                int status = HttpStatus.UNAUTHORIZED.value();

                if (ex instanceof BadCredentialsException) {
                        message = "Usuario o contraseña incorrectos";
                        code = "INVALID_CREDENTIALS";
                } else if (ex instanceof DisabledException) {
                        //Para usuarios desactivados, usar código específico
                        message = "Cuenta desactivada. Contacta al administrador";
                        code = "ACCOUNT_DISABLED";
                        status = HttpStatus.FORBIDDEN.value(); //403
                }

                ErrorResponseDTO error = new ErrorResponseDTO(
                                code,
                                message,
                                status);

                return ResponseEntity
                                .status(status)
                                .body(error);
        }

        // Manejo de IllegalArgumentException
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
                        IllegalArgumentException ex) {

                logger.warn("Illegal argument: {}", ex.getMessage());

                ErrorResponseDTO error = new ErrorResponseDTO(
                                "INVALID_ARGUMENT",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        // Manejo de RuntimeException genérica
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
                        RuntimeException ex) {

                logger.error("Runtime error: {}", ex.getMessage(), ex);

                ErrorResponseDTO error = new ErrorResponseDTO(
                                "RUNTIME_ERROR",
                                "Error inesperado en el servidor",
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }

        // Manejo de excepciones generales no capturadas
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDTO> handleGlobalException(
                        Exception ex, WebRequest request) {

                logger.error("Unhandled error: {}", ex.getMessage(), ex);

                ErrorResponseDTO error = new ErrorResponseDTO(
                                "INTERNAL_SERVER_ERROR",
                                "Ha ocurrido un error interno en el servidor",
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }
}