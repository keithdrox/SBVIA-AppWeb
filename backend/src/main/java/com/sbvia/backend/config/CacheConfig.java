package com.sbvia.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Configuración de caché con Redis.
 *
 * Decisión de diseño (ADR-004):
 * - Se usa RedisCacheManager para que @Cacheable/@CacheEvict usen Redis como backend.
 * - Serialización JSON (GenericJackson2JsonRedisSerializer) en lugar de Java nativa
 *   para evitar ClassCastException entre versiones y poder inspeccionar claves en redis-cli.
 * - TTL global: 10 minutos. TTL específico para "escenarios": 5 minutos (datos semi-estáticos).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final Duration ESCENARIOS_TTL = Duration.ofMinutes(5);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuración por defecto: JSON + TTL 10 min + no cache null values
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        // Configuración específica para la caché de escenarios: TTL 5 min
        RedisCacheConfiguration escenariosConfig = defaultConfig
                .entryTtl(ESCENARIOS_TTL);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        "escenarios", escenariosConfig
                ))
                .build();
    }
}
