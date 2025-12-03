package com.ergpos.app.exception;

/**
 * Excepción para errores de validación de negocio (400 Bad Request).
 * 
 * Usar cuando los datos proporcionados no cumplen con las reglas de negocio.
 * 
 * @example
 * throw new ValidationException("El stock actual no puede ser menor al stock mínimo");
 */
public class ValidationException extends BusinessException {
    
    /**
     * Constructor con mensaje de validación.
     * 
     * @param message Mensaje describiendo la validación fallida
     */
    public ValidationException(String message) {
        super(
            "VALIDATION_ERROR",
            message,
            400
        );
    }
    
    /**
     * Constructor con código de error específico y mensaje.
     * 
     * @param code Código de error específico (ej: "INVALID_PRICE", "STOCK_BELOW_MINIMUM")
     * @param message Mensaje de error
     */
    public ValidationException(String code, String message) {
        super(code, message, 400);
    }
    
    /**
     * Constructor con campo y mensaje.
     * 
     * @param fieldName Campo que falló la validación
     * @param message Mensaje de error
     */
    public static ValidationException forField(String fieldName, String message) {
        return new ValidationException(
            "INVALID_FIELD",
            String.format("Campo '%s': %s", fieldName, message)
        );
    }
}
