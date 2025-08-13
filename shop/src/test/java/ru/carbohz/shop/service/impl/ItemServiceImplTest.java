package ru.carbohz.shop.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.dto.PageableDto;
import ru.carbohz.shop.dto.PageableItemsDto;
import ru.carbohz.shop.exception.ItemNotFoundException;
import ru.carbohz.shop.mapper.ItemMapper;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.SortOption;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {
        ItemMapper.class,
        ItemServiceImpl.class,
})
class ItemServiceImplTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @Autowired
    private ItemServiceImpl itemService;

    @Test
    void findItemById_whenCartPresent() {
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
                })
                .verifyComplete();
    }

    @Test
    void findItemById_whenCartNotPresent() {
        Item item = createItem(1L);

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartRepository.findByItem_Id(1L)).thenReturn(Mono.empty());

        Mono<ItemDto> result = itemService.findItemById(1L);

        StepVerifier.create(result)
                .assertNext(itemDto -> {
                    assertThat(itemDto.getId()).isEqualTo(1L);
                    assertThat(itemDto.getTitle()).isEqualTo("Title 1");
                    assertThat(itemDto.getDescription()).isEqualTo("Description 1");
                    assertThat(itemDto.getImgPath()).isEqualTo("ImagePath 1");
                    assertThat(itemDto.getCount()).isZero();
                    assertThat(itemDto.getPrice()).isEqualTo(1337L);
                })
                .verifyComplete();
    }

    @Test
    void findItemById_whenItemNotFound_throwsItemNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<ItemDto> result = itemService.findItemById(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ItemNotFoundException &&
                        throwable.getMessage().equals("Item with id 1 not found"))
                .verify();
    }

    @Nested
    class GetPageableItems {
        @Test
        void findByTitleContainingOrDescriptionContainingAllIgnoreCase() {
            String search = "Laptop";
            SortOption sortOption = SortOption.NO;
            int pageSize = 20;
            int pageNumber = 1;

            List<Item> items = createItems();

            when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    anyString(), anyInt(), anyLong()))
                    .thenReturn(Flux.fromIterable(items));
            when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString()))
                    .thenReturn(Mono.just(20L));
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

                        // Verify first group
                        List<ItemDto> firstGroup = itemGroups.get(0);
                        assertThat(firstGroup).hasSize(5);
                        assertThat(firstGroup)
                                .isSortedAccordingTo(Comparator.comparingLong(ItemDto::getId));

                        // Verify last group
                        List<ItemDto> lastGroup = itemGroups.get(3);
                        assertThat(lastGroup).hasSize(5);
                        assertThat(lastGroup)
                                .isSortedAccordingTo(Comparator.comparingLong(ItemDto::getId));
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