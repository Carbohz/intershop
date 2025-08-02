package ru.carbohz.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.carbohz.shop.ApiClient;
import ru.carbohz.shop.api.PaymentApi;

@Configuration
public class PaymentServiceConfig {

    @Value("${payment.service.host:localhost}")
    private String host;

    @Value("${payment.service.port:8081}")
    private String port;

    @Bean
    public PaymentApi paymentApi() {
        final String baseUrl = String.format("http://%s:%s", host, port);
        final ApiClient apiClient = new ApiClient().setBasePath(baseUrl);
        return new PaymentApi(apiClient);
    }
}
