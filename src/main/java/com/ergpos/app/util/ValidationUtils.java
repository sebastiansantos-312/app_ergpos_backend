package com.ergpos.app.util;

import com.ergpos.app.exception.ValidationException;

/**
 * Utilidades de validación reutilizables.
 * 
 * Centraliza validaciones comunes para evitar duplicación de código.
 */
public class ValidationUtils {
    
    /**
     * Valida que un string no sea nulo ni vacío.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo (para mensaje de error)
     * @return El valor trimmed
     * @throws ValidationException si el valor es nulo o vacío
     */
    public static String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw ValidationException.forField(fieldName, "es obligatorio");
        }
        return value.trim();
    }
    
    /**
     * Valida que un valor no sea nulo.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo
     * @param <T> Tipo del valor
     * @return El valor si no es nulo
     * @throws ValidationException si el valor es nulo
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw ValidationException.forField(fieldName, "es obligatorio");
        }
        return value;
    }
    
    /**
     * Valida que un número sea positivo.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo
     * @throws ValidationException si el valor es negativo o cero
     */
    public static void requirePositive(Number value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.doubleValue() <= 0) {
            throw ValidationException.forField(fieldName, "debe ser mayor a 0");
        }
    }
    
    /**
     * Valida que un número no sea negativo.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo
     * @throws ValidationException si el valor es negativo
     */
    public static void requireNonNegative(Number value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.doubleValue() < 0) {
            throw ValidationException.forField(fieldName, "no puede ser negativo");
        }
    }
    
    /**
     * Valida que un número esté en un rango.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo
     * @param min Valor mínimo (inclusivo)
     * @param max Valor máximo (inclusivo)
     * @throws ValidationException si el valor está fuera del rango
     */
    public static void requireInRange(Number value, String fieldName, double min, double max) {
        requireNonNull(value, fieldName);
        double val = value.doubleValue();
        if (val < min || val > max) {
            throw ValidationException.forField(fieldName, 
                String.format("debe estar entre %s y %s", min, max));
        }
    }
    
    /**
     * Valida la longitud máxima de un string.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo
     * @param maxLength Longitud máxima permitida
     * @throws ValidationException si excede la longitud
     */
    public static void requireMaxLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw ValidationException.forField(fieldName, 
                String.format("no puede tener más de %d caracteres", maxLength));
        }
    }
    
    /**
     * Valida que un string esté en una lista de valores permitidos.
     * 
     * @param value Valor a validar
     * @param fieldName Nombre del campo
     * @param allowedValues Valores permitidos
     * @throws ValidationException si el valor no está en la lista
     */
    public static void requireOneOf(String value, String fieldName, String... allowedValues) {
        requireNonEmpty(value, fieldName);
        for (String allowed : allowedValues) {
            if (allowed.equalsIgnoreCase(value)) {
                return;
            }
        }
        throw ValidationException.forField(fieldName, 
            String.format("debe ser uno de: %s", String.join(", ", allowedValues)));
    }
    
    /**
     * Valida formato de email básico.
     * 
     * @param email Email a validar
     * @param fieldName Nombre del campo
     * @return Email normalizado (lowercase, trimmed)
     * @throws ValidationException si el formato es inválido
     */
    public static String requireValidEmail(String email, String fieldName) {
        String normalized = requireNonEmpty(email, fieldName).toLowerCase();
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw ValidationException.forField(fieldName, "formato de email inválido");
        }
        return normalized;
    }
    
    private ValidationUtils() {
        // Utility class, no instances
    }
}
