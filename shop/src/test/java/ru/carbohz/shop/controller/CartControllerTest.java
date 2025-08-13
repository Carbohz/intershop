package ru.carbohz.shop.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.CartItemsDto;
import ru.carbohz.shop.model.Action;
import ru.carbohz.shop.service.CartService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
public class CartControllerTest {

    @MockitoBean
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getCartItems() {
        CartItemsDto cartItemsDto = new CartItemsDto();

        when(cartService.getCartItems()).thenReturn(Mono.just(cartItemsDto));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(result -> {
                    String html = result.getResponseBody();
                    assertThat(html).isNotNull();
                    Document doc = Jsoup.parse(html);
                    assertThat(doc.title()).isEqualTo("Корзина товаров");
                });

        verify(cartService, times(1)).getCartItems();
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void changeItemsCount() {
        Long itemId = 1L;
        Action action = Action.DELETE;
        when(cartService.changeItemsInCart(itemId, action)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(UriComponentsBuilder.fromPath("/cart/items/{itemId}")
                        .queryParam("action", action.name())
                        .build(itemId))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(cartService, times(1)).changeItemsInCart(itemId, action);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void buy() {
        Long orderId = 1L;

        when(cartService.createOrder()).thenReturn(Mono.just(orderId));

        String expectedUrl = "/orders/" + orderId + "?newOrder=true";

        webTestClient.post()
                .uri("/cart/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", expectedUrl);

        verify(cartService, times(1)).createOrder();
        verifyNoMoreInteractions(cartService);
    }
}
