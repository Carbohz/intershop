package ru.carbohz.shop.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.carbohz.shop.dto.PageableItemsDto;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.Order;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class RedisConfiguration {
    @Bean
    public RedisCacheManagerBuilderCustomizer cacheCustomizer() {
        return builder -> builder
                .withCacheConfiguration(
                        "items",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.of(10, ChronoUnit.MINUTES))
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair.fromSerializer(
                                                new Jackson2JsonRedisSerializer<>(PageableItemsDto.class)
                                        )
                                )
                )
//                .withCacheConfiguration(
//                        "items",
//                        RedisCacheConfiguration.defaultCacheConfig()
//                                .entryTtl(Duration.of(1, ChronoUnit.MINUTES))
//                                .serializeValuesWith(
//                                        RedisSerializationContext.SerializationPair.fromSerializer(
//                                                new Jackson2JsonRedisSerializer<>(Item.class)
//                                        )
//                                )
//                )
                .withCacheConfiguration(
                        "orders",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.of(1, ChronoUnit.MINUTES))
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair.fromSerializer(
                                                new Jackson2JsonRedisSerializer<>(Order.class)
                                        )
                                )
                );
    }
}
