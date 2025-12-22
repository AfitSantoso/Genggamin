package com.example.genggamin.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Configuration Class
 * Mengkonfigurasi Redis untuk caching data user dan role
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure Redis Template untuk operasi Redis manual jika diperlukan
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Serializer untuk key sebagai String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Serializer untuk value sebagai JSON dengan objectMapper yang sudah dikonfigurasi
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure Cache Manager untuk Spring Cache Abstraction
     * Menggunakan Redis sebagai cache provider
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // Serializer untuk JSON dengan objectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL default 10 menit
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues(); // Tidak cache nilai null

        // Custom cache configurations untuk cache tertentu
        RedisCacheConfiguration userConfig = defaultConfig
                .entryTtl(Duration.ofMinutes(15)); // User cache 15 menit

        RedisCacheConfiguration roleConfig = defaultConfig
                .entryTtl(Duration.ofMinutes(30)); // Role cache 30 menit (jarang berubah)

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("users", userConfig)
                .withCacheConfiguration("userByUsername", userConfig)
                .withCacheConfiguration("roles", roleConfig)
                .build();
    }

    /**
     * ObjectMapper configuration untuk serialization/deserialization REDIS ONLY
     * Mendukung Java Time API dan polymorphic types
     * NOTE: ObjectMapper ini HANYA untuk Redis, tidak untuk HTTP request/response
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register JavaTimeModule untuk support LocalDateTime, etc
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Konfigurasi untuk menghindari error serialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Enable default typing untuk handle polymorphic types dengan aman dan RESTRICTIVE
        // HANYA digunakan untuk Redis cache, tidak untuk HTTP JSON parsing
        // HANYA allow DTO package untuk menghindari serialization issue dengan Hibernate entities
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.genggamin.dto")
                .allowIfSubType(java.util.List.class)
                .allowIfSubType(java.util.Set.class)
                .allowIfSubType(java.util.Map.class)
                .build();
        
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return mapper;
    }
}
