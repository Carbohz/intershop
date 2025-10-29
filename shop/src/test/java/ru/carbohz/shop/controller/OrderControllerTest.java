package ru.carbohz.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.OrderDto;
import ru.carbohz.shop.service.OrderService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebFluxTest(OrderController.class)
public class OrderControllerTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private WebTestClient webTestClient;

    private static final Long USER_ID = 1337L;

    @Test
    public void getOrdersPage() {
        List<OrderDto> orders = new ArrayList<>();

        when(orderService.getOrders(USER_ID)).thenReturn(Flux.fromIterable(orders));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1)).getOrders(USER_ID);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void getOrderPage() {
        long orderId = 1L;
        boolean newOrder = true;
        OrderDto orderDto = new OrderDto();

        when(orderService.getOrderById(orderId, USER_ID)).thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{orderId}")
                        .queryParam("newOrder", newOrder)
                        .build(orderId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String html = result.getResponseBody();
                    // Verify order details are rendered
                    assertThat(html).contains("<h2>Заказ №0</h2>"); // Adjust based on template
                    assertThat(html).contains("<h3>Сумма: null руб.</h3>"); // Verify total
                    // Verify new order indicator if present in template
                    if (newOrder) {
                        assertThat(html).contains("<h1 style=\"text-align:center\">Поздравляем! Успешная покупка! &#128578;</h1>");
                    }
                });

        verify(orderService, times(1)).getOrderById(orderId, USER_ID);
        verifyNoMoreInteractions(orderService);
    }
}
