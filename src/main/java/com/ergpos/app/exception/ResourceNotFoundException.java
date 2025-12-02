package com.ergpos.app.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                "RESOURCE_NOT_FOUND",
                String.format("%s no encontrado: %s", resourceName, identifier),
                404);
    }
}
