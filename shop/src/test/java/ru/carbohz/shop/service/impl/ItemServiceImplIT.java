package ru.carbohz.shop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.config.RedisTestcontainersConfiguration;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import({PostgresTestcontainersConfiguration.class, RedisTestcontainersConfiguration.class})
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

                    System.out.println(redisTemplate.keys("*"));
                    assertThat(redisTemplate.opsForValue().get("item::1"))
                            .isEqualTo(res);

                    Awaitility.await().atMost(2, TimeUnit.SECONDS)
                            .untilAsserted(() -> {
                                assertThat(redisTemplate.opsForValue().get("item::1"))
                                        .isNull();
                            });
                })
                .verifyComplete();
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
