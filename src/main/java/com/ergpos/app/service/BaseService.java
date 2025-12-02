package com.ergpos.app.service;

import com.ergpos.app.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;


public abstract class BaseService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected UUID validateAndParseUUID(String id, String resourceName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "INVALID_ID",
                    String.format("%s ID inválido: %s", resourceName, id),
                    400);
        }
    }

    protected void validateDateRange(LocalDateTime desde, LocalDateTime hasta, String errorMessage) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    errorMessage,
                    400);
        }
    }


    protected <T> T requireNonNull(T obj, String resourceName, String identifier) {
        if (obj == null) {
            throw new BusinessException(
                    "RESOURCE_NOT_FOUND",
                    String.format("%s no encontrado: %s", resourceName, identifier),
                    404);
        }
        return obj;
    }

    protected String requireNonEmpty(String str, String fieldName) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(
                    "INVALID_INPUT",
                    String.format("%s no puede estar vacío", fieldName),
                    400);
        }
        return str.trim();
    }

    protected Integer requirePositive(Integer number, String fieldName) {
        if (number == null || number <= 0) {
            throw new BusinessException(
                    "INVALID_INPUT",
                    String.format("%s debe ser mayor a 0", fieldName),
                    400);
        }
        return number;
    }

    protected <T, R> Page<R> paginateWithSpecification(Specification<T> spec, Pageable pageable,
            Function<T, R> mapper,
            JpaSpecificationExecutor<T> repository) {
        //Usar el método correcto de JpaSpecificationExecutor
        return repository.findAll(spec, pageable).map(mapper);
    }

    protected <T, R> Page<R> paginate(Pageable pageable,
            Function<T, R> mapper,
            PagingAndSortingRepository<T, ?> repository) {
        //Usar PagingAndSortingRepository para findAll(Pageable)
        return repository.findAll(pageable).map(mapper);
    }

    protected <T, R> Page<R> paginateAll(Pageable pageable,
            Function<T, R> mapper,
            org.springframework.data.repository.CrudRepository<T, ?> repository) {
        // Alternativa: convertir a lista y luego a página (NO RECOMENDADO para grandes
        // volúmenes)
        // En realidad, la mayoría de los repositorios JpaRepository ya extienden
        // PagingAndSortingRepository
        throw new UnsupportedOperationException("Este repositorio no soporta paginación directa");
    }

    protected String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException(
                    "INVALID_EMAIL",
                    "Email inválido: " + email,
                    400);
        }
        return normalizedEmail;
    }

    protected String validateRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return null;
        }

        String normalizedRuc = ruc.trim();
        // Validación básica de RUC ecuatoriano (13 dígitos)
        if (!normalizedRuc.matches("^[0-9]{13}$")) {
            throw new BusinessException(
                    "INVALID_RUC",
                    "RUC inválido. Debe tener 13 dígitos",
                    400);
        }
        return normalizedRuc;
    }

    protected String validateTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return null;
        }

        String normalizedTelefono = telefono.trim();
        // Validación básica de teléfono (10 dígitos para Ecuador)
        if (!normalizedTelefono.matches("^[0-9]{10}$")) {
            throw new BusinessException(
                    "INVALID_PHONE",
                    "Teléfono inválido. Debe tener 10 dígitos",
                    400);
        }
        return normalizedTelefono;
    }

    protected void logOperation(String operation, String resource, Object identifier) {
        logger.info("{} - {}: {}", operation, resource, identifier);
    }

    protected void logWarning(String operation, String resource, Object identifier, String message) {
        logger.warn("{} - {}: {} - {}", operation, resource, identifier, message);
    }

    protected void logError(String operation, String resource, Object identifier, Exception e) {
        logger.error("Error en {} - {}: {} - {}", operation, resource, identifier, e.getMessage(), e);
    }

    protected <T extends Comparable<T>> T validateRange(T value, T min, T max, String fieldName) {
        if (value == null) {
            return null;
        }

        if (min != null && value.compareTo(min) < 0) {
            throw new BusinessException(
                    "VALUE_TOO_LOW",
                    String.format("%s debe ser mayor o igual a %s", fieldName, min),
                    400);
        }

        if (max != null && value.compareTo(max) > 0) {
            throw new BusinessException(
                    "VALUE_TOO_HIGH",
                    String.format("%s debe ser menor o igual a %s", fieldName, max),
                    400);
        }

        return value;
    }

    /**
     * Valida que un BigDecimal sea positivo
     */
    protected java.math.BigDecimal validatePositiveBigDecimal(java.math.BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }

        if (value.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "INVALID_VALUE",
                    String.format("%s debe ser mayor a 0", fieldName),
                    400);
        }

        return value;
    }

    /**
     * Valida longitud de string
     */
    protected String validateLength(String value, int maxLength, String fieldName) {
        if (value == null) {
            return null;
        }

        if (value.length() > maxLength) {
            throw new BusinessException(
                    "STRING_TOO_LONG",
                    String.format("%s no puede tener más de %d caracteres", fieldName, maxLength),
                    400);
        }

        return value;
    }
}