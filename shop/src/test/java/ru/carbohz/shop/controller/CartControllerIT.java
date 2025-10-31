package ru.carbohz.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.shop.ApiClient;
import ru.carbohz.shop.api.PaymentApi;
import ru.carbohz.shop.api.model.BalancePostRequest;
import ru.carbohz.shop.config.KeycloakTestcontainersConfiguration;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.config.RedisTestcontainersConfiguration;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.Order;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;
import ru.carbohz.shop.repository.OrderRepository;
import ru.carbohz.shop.repository.UserRepository;
import ru.carbohz.shop.service.OAuth2Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestcontainersConfiguration.class,
        RedisTestcontainersConfiguration.class,
        KeycloakTestcontainersConfiguration.class})
@AutoConfigureWebTestClient
public class CartControllerIT {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private PaymentApi paymentApi;

    @MockitoBean
    private OAuth2Service oAuth2Service;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri",
                () -> "http://localhost:%d/realms/master".formatted(KeycloakTestcontainersConfiguration.keycloak.getMappedPort(8080)));
    }

    @Test
    public void buy() {
        // save user
        User user = new User("admin", "admin");
        User saved = userRepository.save(user).block();
        assertThat(saved).isNotNull();

        // Setup test data
        Item item1 = itemRepository.findAll().blockFirst();
        assertThat(item1).isNotNull();
        Item item2 = itemRepository.findAll().blockLast();
        assertThat(item2).isNotNull();

        Cart cart1 = new Cart();
        cart1.setItemId(item1.getId());
        cart1.setUserId(saved.getId());
        cart1.setCount(2L);
        cartRepository.save(cart1).block();

        Cart cart2 = new Cart();
        cart2.setItemId(item2.getId());
        cart2.setUserId(saved.getId());
        cart2.setCount(10L);
        cartRepository.save(cart2).block();

        ResponseEntity<Void> response = ResponseEntity.ok().build();
        when(paymentApi.balancePostWithHttpInfo(any(BalancePostRequest.class)))
                .thenReturn(Mono.just(response));
        final String accessToken = "token";
        when(oAuth2Service.getTokenValue()).thenReturn(Mono.just(accessToken));
        ApiClient apiClient = mock(ApiClient.class);
        when(paymentApi.getApiClient()).thenReturn(apiClient);
        when(apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)).thenReturn(apiClient);

        // Execute request
        webTestClient
                .mutateWith(mockUser(saved))
                .post()
                .uri("/cart/buy")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.empty())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");

        // Verify database state
        Mono<Long> cartCount = cartRepository.count();
        StepVerifier.create(cartCount)
                .expectNext(0L)
                .verifyComplete();

        Mono<Order> orderMono = orderRepository.findAll().next();
        StepVerifier.create(orderMono)
                .assertNext(order -> {
                    assertThat(order.getId()).isEqualTo(1L);
                    assertThat(order.getTotalSum()).isEqualTo(359988L);
                })
                .verifyComplete();
    }

    @Test
    public void checkOrderPageRendering() {
        // save user
        User user = new User("admin", "admin");
        User saved = userRepository.save(user).block();
        assertThat(saved).isNotNull();

        // First create an order
        Item item = itemRepository.findAll().blockFirst();
        Order order = new Order();
        order.setUserId(saved.getId());
        order.setTotalSum(item.getPrice() * 2);
        orderRepository.save(order).block();

        // Test the order page rendering
        webTestClient
                .mutateWith(mockUser(saved))
                .get()
                .uri("/orders/{orderId}?newOrder=true", order.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String html = new String(response.getResponseBody());
                    assertThat(html).contains("Поздравляем! Успешная покупка!");
                    assertThat(html).contains("Заказ №" + order.getId());
                    assertThat(html).contains("Сумма: " + order.getTotalSum() + " руб.");
                    // Add more HTML assertions as needed
                });
    }
}
