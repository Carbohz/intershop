package ru.carbohz.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PaymentBalanceConfig {
    @Value("${balance.sum:100000}")
    private BigDecimal balance;

    @Bean
    public BigDecimal getBalance() {
        return balance;
    }
}
