package com.sbvia.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Servicio para generar y validar tokens JWT usando jjwt 0.12.x.
 * Firma tokens con HS256 y una clave secreta de al menos 256 bits.
 * Cada token incluye un JTI (JWT ID) único para soporte de blacklist en Redis.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms}")
    private long accessExpirationMs;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${security.jwt.audience}")
    private String jwtAudience;

    /**
     * Genera un access token JWT con claims personalizados.
     */
    public String generateAccessToken(UserDetails userDetails, Long userId, String rol) {
        return buildToken(
                Map.of(
                        "email", userDetails.getUsername(),
                        "rol", rol,
                        "type", "access"
                ),
                String.valueOf(userId),
                accessExpirationMs
        );
    }

    /**
     * Genera un refresh token JWT.
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        return buildToken(
                Map.of("type", "refresh"),
                String.valueOf(userId),
                refreshExpirationMs
        );
    }

    /**
     * Construye un token JWT firmado con HS256.
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .issuer(jwtIssuer)
                .subject(subject)
                .audience().add(jwtAudience).and()
                .id(UUID.randomUUID().toString())
                .notBefore(now)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrae el subject (ID del usuario) del token.
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el email del token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extrae el JTI (JWT ID) del token — usado para blacklist en Redis.
     */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extrae la fecha de expiración del token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    public java.util.Set<String> extractAudience(String token) {
        return extractClaim(token, Claims::getAudience);
    }

    public Date extractNotBefore(String token) {
        return extractClaim(token, Claims::getNotBefore);
    }

    /**
     * Extrae el tipo de token (access o refresh).
     */
    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    /**
     * Valida que el token sea válido: firma correcta, no expirado,
     * y que el username coincida con el UserDetails.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return (email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtiene el tiempo restante de expiración en milisegundos.
     * Usado para configurar el TTL en Redis al revocar el token.
     */
    public long getExpirationRemainingMs(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .requireIssuer(jwtIssuer)
                .requireAudience(jwtAudience)
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
