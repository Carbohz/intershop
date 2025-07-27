package ru.carbohz.intershop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.intershop.TestcontainersConfiguration;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class CartControllerIT {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void buy() {
        // Setup test data
        Item item1 = itemRepository.findAll().blockFirst();
        assertThat(item1).isNotNull();
        Item item2 = itemRepository.findAll().blockLast();
        assertThat(item2).isNotNull();

        Cart cart1 = new Cart();
        cart1.setItemId(item1.getId());
        cart1.setCount(2L);
        cartRepository.save(cart1).block();

        Cart cart2 = new Cart();
        cart2.setItemId(item2.getId());
        cart2.setCount(10L);
        cartRepository.save(cart2).block();

        // Execute request
        webTestClient.post()
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
        // First create an order
        Item item = itemRepository.findAll().blockFirst();
        Order order = new Order();
        order.setTotalSum(item.getPrice() * 2);
        orderRepository.save(order).block();

        // Test the order page rendering
        webTestClient.get()
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
