package com.ergpos.app.exception;

public class StockInsufficiencyException extends BusinessException {
    public StockInsufficiencyException(int disponible, int solicitado) {
        super(
                "INSUFFICIENT_STOCK",
                String.format("Stock insuficiente. Disponible: %d, solicitado: %d", disponible, solicitado),
                400);
    }
}