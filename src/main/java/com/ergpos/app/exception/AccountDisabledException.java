package com.ergpos.app.exception;

public class AccountDisabledException extends BusinessException {
    public AccountDisabledException() {
        super(
                "ACCOUNT_DISABLED",
                "Tu cuenta ha sido desactivada. Contacta al administrador.",
                403);
    }
}