package com.ergpos.app.util;

import java.text.Normalizer;
import java.util.UUID;

public class StringUtils {

    /**
     * Normaliza un string removiendo acentos y caracteres especiales
     * 
     * @param input String a normalizar
     * @return String normalizado en mayúsculas
     */
    public static String normalizeForCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Remover acentos usando Normalizer
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); // Remover marcas diacríticas

        // Convertir a mayúsculas y reemplazar espacios por guiones bajos
        normalized = normalized.toUpperCase()
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Z0-9_]", ""); // Solo letras, números y _

        return normalized;
    }

    /**
     * Genera un código único para una entidad
     * 
     * @param prefix    Prefijo del código (ej: "CAT", "PROD")
     * @param baseName  Nombre base para generar el código
     * @param maxLength Longitud máxima del código
     * @return Código generado
     */
    public static String generateCode(String prefix, String baseName, int maxLength) {
        String normalizedBase = normalizeForCode(baseName);

        if (normalizedBase.isEmpty()) {
            // Si no hay nombre válido, usar UUID
            return prefix + "-" + UUID.randomUUID().toString()
                    .substring(0, Math.min(8, maxLength - prefix.length() - 1))
                    .toUpperCase();
        }

        String code = prefix + "-" + normalizedBase;

        // Truncar si excede la longitud máxima
        if (code.length() > maxLength) {
            code = code.substring(0, maxLength);
        }

        return code;
    }

    /**
     * Valida que un código tenga el formato correcto
     * 
     * @param code Código a validar
     * @return true si es válido
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        // Debe contener solo letras mayúsculas, números, guiones y guiones bajos
        return code.matches("^[A-Z0-9_-]+$");
    }
}