package com.sbvia.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void guardaElJtiRevocadoConSuTiempoDeVida() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.blacklistToken("token-123", 45_000);

        verify(valueOperations).set(
                "jwt:blacklist:token-123",
                "revoked",
                45_000,
                TimeUnit.MILLISECONDS
        );
    }

    @Test
    void informaCuandoElTokenEstaRevocado() {
        when(redisTemplate.hasKey("jwt:blacklist:token-123")).thenReturn(true);

        assertThat(tokenBlacklistService.isTokenBlacklisted("token-123")).isTrue();
    }

    @Test
    void trataUnaRespuestaNulaDeRedisComoTokenVigente() {
        when(redisTemplate.hasKey("jwt:blacklist:token-123")).thenReturn(null);

        assertThat(tokenBlacklistService.isTokenBlacklisted("token-123")).isFalse();
    }
}
