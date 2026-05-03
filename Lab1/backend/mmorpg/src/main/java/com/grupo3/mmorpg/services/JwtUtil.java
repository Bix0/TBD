package com.grupo3.mmorpg.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para generar y validar tokens JWT.
 * Usa firma HMAC-SHA256 con una clave secreta segura.
 */
@Component
public class JwtUtil {

    // Clave secreta segura generada automáticamente para HMAC-SHA256
    // En producción, esto debería venir de application.properties
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long EXPIRATION_TIME = 86400000L; // 24 horas en milisegundos

    /**
     * Genera un token JWT para un usuario autenticado.
     * 
     * @param idJugador ID del jugador
     * @param username  Nombre de usuario
     * @param rol       Rol del usuario (Admin, Usuario)
     * @return Token JWT como String
     */
    public String generateToken(Long idJugador, String username, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", idJugador);
        claims.put("rol", rol);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Extrae el username (subject) del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del jugador del token.
     */
    public Long extractId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    /**
     * Extrae el rol del token.
     */
    public String extractRol(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    /**
     * Valida el token comparando el username y la expiración.
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
