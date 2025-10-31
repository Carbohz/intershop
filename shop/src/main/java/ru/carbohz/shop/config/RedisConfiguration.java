package ru.carbohz.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.dto.PageableItemsDto;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Value("${caching.ttl.items:60}")
    private Long itemsTtl;

    @Value("${caching.ttl.item:60}")
    private Long itemTtl;

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheCustomizer() {
        log.info("using items ttl: {}, item ttl: {}", itemsTtl, itemTtl);
        return builder -> builder
                .withCacheConfiguration(
                        "items",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.of(itemsTtl, ChronoUnit.SECONDS))
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair.fromSerializer(
                                                new Jackson2JsonRedisSerializer<>(PageableItemsDto.class)
                                        )
                                )
                )
                .withCacheConfiguration(
                        "item",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.of(itemTtl, ChronoUnit.SECONDS))
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair.fromSerializer(
                                                new Jackson2JsonRedisSerializer<>(ItemDto.class)
                                        )
                                )
                );
    }
}
