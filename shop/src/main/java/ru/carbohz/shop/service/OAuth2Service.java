package ru.carbohz.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OAuth2Service {
    private final ReactiveOAuth2AuthorizedClientManager manager;

    public Mono<String> getTokenValue() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("keycloak-rest-client")
                        .principal("system")
                        .build()
                )
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
