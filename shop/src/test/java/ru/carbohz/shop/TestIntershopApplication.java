package ru.carbohz.shop;

import org.springframework.boot.SpringApplication;

public class TestIntershopApplication {

	public static void main(String[] args) {
		SpringApplication.from(ShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
