package com.ergpos.app.exception;

/**
 * Excepción para recursos no encontrados (404 Not Found).
 * 
 * Usar cuando un recurso solicitado no existe en la base de datos.
 * 
 * @example
 * throw new ResourceNotFoundException("Usuario", "email", "user@example.com");
 */
public class ResourceNotFoundException extends BusinessException {
    
    /**
     * Constructor con tipo de recurso y identificador.
     * 
     * @param resourceName Tipo de recurso (ej: "Usuario", "Producto")
     * @param identifier Identificador del recurso
     */
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                "RESOURCE_NOT_FOUND",
                String.format("%s no encontrado: %s", resourceName, identifier),
                404);
    }
    
    /**
     * Constructor con tipo de recurso, campo y valor.
     * 
     * @param resourceType Tipo de recurso (ej: "Usuario", "Producto")
     * @param fieldName Campo usado para buscar (ej: "email", "codigo")
     * @param fieldValue Valor buscado
     */
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(
            "RESOURCE_NOT_FOUND",
            String.format("%s no encontrado con %s: %s", resourceType, fieldName, fieldValue),
            404
        );
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje de error personalizado
     */
    public ResourceNotFoundException(String message) {
        super(
            "RESOURCE_NOT_FOUND",
            message,
            404
        );
    }
}
