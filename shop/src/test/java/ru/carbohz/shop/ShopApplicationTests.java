package ru.carbohz.shop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.carbohz.shop.config.KeycloakTestcontainersConfiguration;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.config.RedisTestcontainersConfiguration;

@Import({PostgresTestcontainersConfiguration.class, RedisTestcontainersConfiguration.class, KeycloakTestcontainersConfiguration.class})
@SpringBootTest
class ShopApplicationTests {

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri",
				() -> "http://localhost:%d/realms/master".formatted(KeycloakTestcontainersConfiguration.keycloak.getMappedPort(8080)));
	}

	@Test
	void contextLoads() {
	}

}
