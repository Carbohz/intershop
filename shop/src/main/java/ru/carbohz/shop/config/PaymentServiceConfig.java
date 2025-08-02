package ru.carbohz.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.carbohz.shop.ApiClient;
import ru.carbohz.shop.api.PaymentApi;

@Configuration
public class PaymentServiceConfig {

    @Bean
    public PaymentApi paymentApi() {
        final ApiClient apiClient = new ApiClient().setBasePath("http://localhost:8081/");
        return new PaymentApi(apiClient);
    }
}
