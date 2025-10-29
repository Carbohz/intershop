package ru.carbohz.shop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.shop.config.KeycloakTestcontainersConfiguration;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.config.RedisTestcontainersConfiguration;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.dto.PageableDto;
import ru.carbohz.shop.dto.PageableItemsDto;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.SortOption;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import({PostgresTestcontainersConfiguration.class, RedisTestcontainersConfiguration.class, KeycloakTestcontainersConfiguration.class})
@ActiveProfiles("test")
public class ItemServiceImplIT {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        int foo = 42;
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri",
                () -> "http://localhost:%d/realms/master".formatted(KeycloakTestcontainersConfiguration.keycloak.getMappedPort(8080)));
    }

    @Test
    void findItemById_isCached() {
        Item item = createItem(1L);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setCount(2L);
        cart.setItemId(item.getId());

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartRepository.findByItem_Id(1L)).thenReturn(Mono.just(cart));

        Mono<ItemDto> result = itemService.findItemById(1L);

        StepVerifier.create(result)
                .assertNext(itemDto -> {
                    assertThat(itemDto.getId()).isEqualTo(1L);
                    assertThat(itemDto.getTitle()).isEqualTo("Title 1");
                    assertThat(itemDto.getDescription()).isEqualTo("Description 1");
                    assertThat(itemDto.getImgPath()).isEqualTo("ImagePath 1");
                    assertThat(itemDto.getCount()).isEqualTo(2L);
                    assertThat(itemDto.getPrice()).isEqualTo(1337L);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String res;
                    try {
                        res = objectMapper.writeValueAsString(itemDto);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    assertThat(redisTemplate.opsForValue().get("item::1"))
                            .isEqualTo(res);

                    await().atMost(3, TimeUnit.SECONDS)
                            .untilAsserted(() -> {
                                assertThat(redisTemplate.opsForValue().get("item::1"))
                                        .isNull();
                            });
                })
                .verifyComplete();
    }

    @Test
    void findAll() {
        String search = "";
        SortOption sortOption = SortOption.NO;
        int pageSize = 20;
        int pageNumber = 1;

        List<Item> items = createItems();

        when(itemRepository.findAll(anyInt(), anyLong()))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count()).thenReturn(Mono.just(20L));
        when(cartRepository.findByItem_Id(anyLong())).thenReturn(Mono.empty());

        Mono<PageableItemsDto> result = itemService.getPageableItems(search, sortOption, pageSize, pageNumber);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    PageableDto paging = dto.getPageable();
                    assertThat(paging.pageNumber()).isEqualTo(pageNumber);
                    assertThat(paging.pageSize()).isEqualTo(pageSize);
                    assertThat(paging.hasNext()).isFalse();
                    assertThat(paging.hasPrevious()).isFalse();

                    List<List<ItemDto>> itemGroups = dto.getItems();
                    assertThat(itemGroups).hasSize(4);

                    // Verify first item
                    ItemDto firstDto = itemGroups.get(0).get(0);
                    assertThat(firstDto.getId()).isEqualTo(1L);
                    assertThat(firstDto.getTitle()).isEqualTo("Title 1");
                    assertThat(firstDto.getPrice()).isEqualTo(1337L);

                    // Verify last item
                    ItemDto lastDto = itemGroups.get(3).get(4);
                    assertThat(lastDto.getId()).isEqualTo(20L);
                    assertThat(lastDto.getTitle()).isEqualTo("Title 20");
                    assertThat(lastDto.getPrice()).isEqualTo(1337L * 20L);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String res;
                    try {
                        res = objectMapper.writeValueAsString(dto);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    String key = "items::,NO,20,1";
                    assertThat(redisTemplate.opsForValue().get(key))
                            .isEqualTo(res);

                    await().atMost(3, TimeUnit.SECONDS)
                            .untilAsserted(() -> {
                                assertThat(redisTemplate.opsForValue().get(key))
                                        .isNull();
                            });
                })
                .verifyComplete();
    }

    private List<Item> createItems() {
        List<Item> items = new ArrayList<>();
        for (long i = 1; i <= 20L; i++) {
            items.add(createItem(i));
        }
        return items;
    }

    private Item createItem(long id) {
        Item item = new Item();
        item.setId(id);
        item.setTitle("Title " + id);
        item.setDescription("Description " + id);
        item.setImagePath("ImagePath " + id);
        item.setPrice(1337L * id);
        return item;
    }
}
