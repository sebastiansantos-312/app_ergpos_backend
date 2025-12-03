package com.ergpos.app.exception;

/**
 * Excepción para recursos duplicados (409 Conflict).
 * 
 * Usar cuando se intenta crear un recurso que ya existe.
 * 
 * @example
 * throw new DuplicateResourceException("Usuario", "email", "user@example.com");
 */
public class DuplicateResourceException extends BusinessException {
    
    /**
     * Constructor con tipo de recurso, campo y valor duplicado.
     * 
     * @param resourceType Tipo de recurso (ej: "Usuario", "Producto")
     * @param fieldName Campo duplicado (ej: "email", "codigo")
     * @param fieldValue Valor duplicado
     */
    public DuplicateResourceException(String resourceType, String fieldName, Object fieldValue) {
        super(
            "DUPLICATE_RESOURCE",
            String.format("Ya existe un %s con %s: %s", resourceType, fieldName, fieldValue),
            409
        );
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje de error personalizado
     */
    public DuplicateResourceException(String message) {
        super(
            "DUPLICATE_RESOURCE",
            message,
            409
        );
    }
    
    /**
     * Constructor con código de error personalizado y mensaje.
     * 
     * @param code Código de error específico
     * @param message Mensaje de error
     */
    public DuplicateResourceException(String code, String message) {
        super(code, message, 409);
    }
}
