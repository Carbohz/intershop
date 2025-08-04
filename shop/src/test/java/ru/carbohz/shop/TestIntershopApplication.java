package ru.carbohz.shop;

import org.springframework.boot.SpringApplication;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.config.RedisTestcontainersConfiguration;

public class TestIntershopApplication {

	public static void main(String[] args) {
		SpringApplication.from(ShopApplication::main)
				.with(PostgresTestcontainersConfiguration.class, RedisTestcontainersConfiguration.class)
				.run(args);
	}

}
