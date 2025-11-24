package com.ergpos.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    // Secret robusto por defecto (64 caracteres)
    private static final String DEFAULT_JWT_SECRET = "ErgPos2024!SuperSecureJWTKey@InventorySystem#256BitStrength$ForProduction";

    @Value("${app.jwt.secret:" + DEFAULT_JWT_SECRET + "}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}") // 24 horas por defecto
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        // Validar y usar el secret más robusto
        String effectiveSecret = (jwtSecret != null && jwtSecret.length() >= 32)
                ? jwtSecret
                : DEFAULT_JWT_SECRET;

        return Keys.hmacShaKeyFor(effectiveSecret.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // En caso de token expirado, aún podemos extraer el username
            return e.getClaims().getSubject();
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Token JWT inválido: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Token JWT expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Token JWT no soportado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Claims JWT vacíos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error validando JWT: " + e.getMessage());
        }
        return false;
    }

    // Método para obtener roles del token
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("roles", List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    // Método para verificar si el token está a punto de expirar (opcional)
    public boolean isTokenExpiringSoon(String token, int minutesThreshold) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();

            return timeUntilExpiration <= (minutesThreshold * 60 * 1000);
        } catch (Exception e) {
            return true; // Si hay error, considerar como expirado
        }
    }
}