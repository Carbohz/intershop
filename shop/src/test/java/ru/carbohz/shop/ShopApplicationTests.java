package ru.carbohz.shop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.config.RedisTestcontainersConfiguration;

@Import({PostgresTestcontainersConfiguration.class, RedisTestcontainersConfiguration.class})
@SpringBootTest
class ShopApplicationTests {

	@Test
	void contextLoads() {
	}

}
