package ru.carbohz.intershop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.service.CartService;
import ru.carbohz.intershop.service.ItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
public class ItemControllerTest {

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void showItems() {
        String search = "random";
        SortOption sort = SortOption.PRICE;
        int pageSize = 5;
        int pageNumber = 2;

        PageableItemsDto pageableItems = new PageableItemsDto();
        PageableDto pageableDto = new PageableDto();
        pageableDto.setPageSize(pageSize);
        pageableDto.setPageNumber(pageNumber);
        pageableItems.setPageable(pageableDto);

        when(itemService.getPageableItems(search, sort, pageSize, pageNumber))
                .thenReturn(Mono.just(pageableItems));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/main/items")
                        .queryParam("search", search)
                        .queryParam("sort", sort)
                        .queryParam("pageSize", pageSize)
                        .queryParam("pageNumber", pageNumber)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String html = result.getResponseBody();
                    assertThat(html).contains("<label for=\"search\">");
                    assertThat(html).contains("<label for=\"sort\">");
                    assertThat(html).contains("<label for=\"pageSize\">");
                });


        verify(itemService).getPageableItems(search, sort, pageSize, pageNumber);
        verifyNoMoreInteractions(itemService);
        verifyNoInteractions(cartService);
    }

    @Test
    public void addItemToCartFromMainPage() {
        Long itemId = 1L;
        Action action = Action.PLUS;
        when(cartService.changeItemsInCart(itemId, action)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/main/items/{itemId}")
                        .queryParam("action", action)
                        .build(itemId))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main/items");

        verify(cartService, times(1)).changeItemsInCart(itemId, action);
        verifyNoMoreInteractions(cartService);
        verifyNoInteractions(itemService);
    }

    @Test
    public void getItemById() {
        Long itemId = 1L;
        ItemDto itemDto = new ItemDto();
        itemDto.setTitle("Test item");
        when(itemService.findItemById(itemId)).thenReturn(Mono.just(itemDto));

        webTestClient.get()
                .uri("/items/{itemId}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String html = result.getResponseBody();
                    assertThat(html).contains("<b>Test item</b>"); // Verify item is rendered
                });

        verify(itemService, times(1)).findItemById(itemId);
        verifyNoMoreInteractions(itemService);
        verifyNoInteractions(cartService);
    }

    @Test
    public void addItemToCartFromItemPage() {
        Long itemId = 1L;
        Action action = Action.MINUS;

        when(cartService.changeItemsInCart(itemId, action))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/items/{itemId}")
                        .queryParam("action", action)
                        .build(itemId))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/" + itemId);

        verify(cartService, times(1)).changeItemsInCart(itemId, action);
        verifyNoMoreInteractions(cartService);
        verifyNoInteractions(itemService);
    }
}
