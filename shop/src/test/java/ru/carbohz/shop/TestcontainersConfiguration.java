package ru.carbohz.shop;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.4"));

	@Container
	private static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:8-alpine"));

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return postgreSQLContainer;
	}

	@Bean
	@ServiceConnection
	RedisContainer redisContainer() {
		return redisContainer;
	}
}
