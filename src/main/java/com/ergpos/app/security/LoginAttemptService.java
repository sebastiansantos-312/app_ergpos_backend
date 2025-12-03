package com.ergpos.app.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para prevenir ataques de fuerza bruta mediante rate limiting.
 * 
 * Rastrea intentos de login fallidos por IP y bloquea temporalmente
 * después de exceder el límite.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = TimeUnit.MINUTES.toMillis(15); // 15 minutos

    private final ConcurrentHashMap<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Registra un intento de login fallido.
     * 
     * @param key Identificador (generalmente IP del cliente)
     */
    public void loginFailed(String key) {
        LoginAttempt attempt = attemptsCache.computeIfAbsent(key, k -> new LoginAttempt());
        attempt.incrementAttempts();
    }

    /**
     * Registra un login exitoso y limpia los intentos fallidos.
     * 
     * @param key Identificador (generalmente IP del cliente)
     */
    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    /**
     * Verifica si una IP está bloqueada por exceso de intentos.
     * 
     * @param key Identificador (generalmente IP del cliente)
     * @return true si está bloqueada, false si puede intentar
     */
    public boolean isBlocked(String key) {
        LoginAttempt attempt = attemptsCache.get(key);

        if (attempt == null) {
            return false;
        }

        // Si ha pasado el tiempo de bloqueo, limpiar
        if (attempt.isExpired()) {
            attemptsCache.remove(key);
            return false;
        }

        return attempt.getAttempts() >= MAX_ATTEMPTS;
    }

    /**
     * Obtiene el número de intentos fallidos.
     * 
     * @param key Identificador
     * @return Número de intentos
     */
    public int getAttempts(String key) {
        LoginAttempt attempt = attemptsCache.get(key);
        return attempt != null ? attempt.getAttempts() : 0;
    }

    /**
     * Obtiene el tiempo restante de bloqueo en minutos.
     * 
     * @param key Identificador
     * @return Minutos restantes de bloqueo, 0 si no está bloqueado
     */
    public long getRemainingLockTime(String key) {
        LoginAttempt attempt = attemptsCache.get(key);

        if (attempt == null || !isBlocked(key)) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - attempt.getLastAttemptTime();
        long remaining = LOCK_TIME_DURATION - elapsed;

        return TimeUnit.MILLISECONDS.toMinutes(remaining);
    }

    /**
     * Limpia manualmente los intentos de una IP (útil para administradores).
     * 
     * @param key Identificador a limpiar
     */
    public void clearAttempts(String key) {
        attemptsCache.remove(key);
    }

    /**
     * Clase interna para rastrear intentos de login.
     */
    private static class LoginAttempt {
        private int attempts = 0;
        private long lastAttemptTime = System.currentTimeMillis();

        public void incrementAttempts() {
            attempts++;
            lastAttemptTime = System.currentTimeMillis();
        }

        public int getAttempts() {
            return attempts;
        }

        public long getLastAttemptTime() {
            return lastAttemptTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastAttemptTime > LOCK_TIME_DURATION;
        }
    }
}
