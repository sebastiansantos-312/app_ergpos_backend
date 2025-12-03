package com.ergpos.app.util;

import com.ergpos.app.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Validador de contraseñas con reglas de seguridad.
 * 
 * Implementa las mejores prácticas de OWASP para validación de contraseñas.
 */
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    // Patrones para validación
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    // Contraseñas comunes a rechazar
    private static final String[] COMMON_PASSWORDS = {
            "password", "123456", "12345678", "qwerty", "abc123",
            "monkey", "1234567", "letmein", "trustno1", "dragon",
            "baseball", "iloveyou", "master", "sunshine", "ashley",
            "bailey", "passw0rd", "shadow", "123123", "654321"
    };

    /**
     * Valida una contraseña según las reglas de seguridad.
     * 
     * Reglas:
     * - Longitud mínima: 8 caracteres
     * - Longitud máxima: 128 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un dígito
     * - Al menos un carácter especial
     * - No puede ser una contraseña común
     * 
     * @param password Contraseña a validar
     * @throws ValidationException si la contraseña no cumple los requisitos
     */
    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("INVALID_PASSWORD", "La contraseña es obligatoria");
        }

        // Validar longitud
        if (password.length() < MIN_LENGTH) {
            throw new ValidationException("WEAK_PASSWORD",
                    String.format("La contraseña debe tener al menos %d caracteres", MIN_LENGTH));
        }

        if (password.length() > MAX_LENGTH) {
            throw new ValidationException("INVALID_PASSWORD",
                    String.format("La contraseña no puede tener más de %d caracteres", MAX_LENGTH));
        }

        // Validar complejidad
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new ValidationException("WEAK_PASSWORD",
                    "La contraseña debe contener al menos una letra mayúscula");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new ValidationException("WEAK_PASSWORD",
                    "La contraseña debe contener al menos una letra minúscula");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new ValidationException("WEAK_PASSWORD",
                    "La contraseña debe contener al menos un número");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new ValidationException("WEAK_PASSWORD",
                    "La contraseña debe contener al menos un carácter especial (!@#$%^&*(),.?\":{}|<>)");
        }

        // Validar que no sea una contraseña común
        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.contains(common)) {
                throw new ValidationException("WEAK_PASSWORD",
                        "La contraseña es demasiado común. Por favor elige una más segura");
            }
        }
    }

    /**
     * Valida una contraseña con reglas más flexibles (solo longitud mínima).
     * Útil para migración de sistemas legacy.
     * 
     * @param password Contraseña a validar
     * @throws ValidationException si la contraseña no cumple los requisitos mínimos
     */
    public static void validateBasic(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("INVALID_PASSWORD", "La contraseña es obligatoria");
        }

        if (password.length() < MIN_LENGTH) {
            throw new ValidationException("WEAK_PASSWORD",
                    String.format("La contraseña debe tener al menos %d caracteres", MIN_LENGTH));
        }

        if (password.length() > MAX_LENGTH) {
            throw new ValidationException("INVALID_PASSWORD",
                    String.format("La contraseña no puede tener más de %d caracteres", MAX_LENGTH));
        }
    }

    private PasswordValidator() {
        // Utility class, no instances
    }
}
