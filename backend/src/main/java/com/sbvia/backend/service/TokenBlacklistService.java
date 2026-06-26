package com.sbvia.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Servicio para gestionar la blacklist de tokens JWT en Redis.
 * Almacena los JTI (JWT ID) de tokens revocados con TTL igual
 * a la expiración del token, para que se auto-eliminen de Redis.
 *
 * Decisión documentada en ADR-003: jwt-redis.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * Agrega un JTI a la blacklist de Redis con TTL en milisegundos.
     * Cuando el TTL expira, Redis elimina la entrada automáticamente.
     */
    public void blacklistToken(String jti, long expirationMs) {
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "revoked", expirationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Verifica si un JTI está en la blacklist.
     * Consultado por JwtAuthFilter en cada solicitud antes de autorizar.
     */
    public boolean isTokenBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
