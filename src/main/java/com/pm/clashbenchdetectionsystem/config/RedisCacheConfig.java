package com.pm.clashbenchdetectionsystem.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String CACHE_CLAN = "clan";
    public static final String CACHE_PLAYER = "player";
    public static final String CACHE_CWL_SEASON = "cwlSeason";
    public static final String CACHE_CWL_WAR = "cwlWar";
    public static final String CACHE_LEADERBOARD = "leaderboard";
    public static final String CACHE_PLAYER_CWL_HISTORY = "playerCwlHistory";
    public static final String CACHE_PLAYER_STATS = "playerStats";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .build();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Clan & player profiles: 30-min TTL — popular clans stay hot via LFU eviction
        RedisCacheConfiguration profileConfig = defaultConfig.entryTtl(Duration.ofMinutes(30));

        // CWL data: 24-hour TTL — immutable once ended, targeted eviction handles active data
        RedisCacheConfiguration cwlConfig = defaultConfig.entryTtl(Duration.ofHours(24));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                CACHE_CLAN, profileConfig,
                CACHE_PLAYER, profileConfig,
                CACHE_CWL_SEASON, cwlConfig,
                CACHE_CWL_WAR, cwlConfig,
                CACHE_LEADERBOARD, cwlConfig,
                CACHE_PLAYER_CWL_HISTORY, profileConfig,
                CACHE_PLAYER_STATS, profileConfig
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
