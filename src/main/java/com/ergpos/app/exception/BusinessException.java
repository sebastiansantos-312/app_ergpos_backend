
package com.ergpos.app.exception;

public class BusinessException extends RuntimeException {
    private String code;
    private int statusCode;

    public BusinessException(String code, String message, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}