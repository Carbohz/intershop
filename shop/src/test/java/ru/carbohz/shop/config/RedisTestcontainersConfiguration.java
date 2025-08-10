package ru.carbohz.shop.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class RedisTestcontainersConfiguration {

    @Container
    private static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.4.2-bookworm"));

    @Bean
    @ServiceConnection
    RedisContainer redisContainer() {
        return redisContainer;
    }
}
