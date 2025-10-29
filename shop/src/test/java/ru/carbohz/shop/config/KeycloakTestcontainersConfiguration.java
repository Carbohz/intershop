package ru.carbohz.shop.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class KeycloakTestcontainersConfiguration {

    @Container
    public static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.3.2")
            .withRealmImportFile("keycloak/realm-export.json");

    @Bean
    KeycloakContainer keycloakContainer() {
        return keycloak;
    }
}
