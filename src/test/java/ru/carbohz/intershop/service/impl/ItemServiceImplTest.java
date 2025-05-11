package ru.carbohz.intershop.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.exception.ItemNotFoundException;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.service.PageableService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        ItemServiceImpl.class,
        ItemMapper.class,
        PageableService.class,
})
class ItemServiceImplTest {

    @Autowired
    ItemServiceImpl itemService;

    @MockitoBean
    ItemRepository itemRepository;

    @MockitoBean
    CartRepository cartRepository;

    @Test
    void findItemById_whenCartPresent() {
        Item item = createItem();

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setCount(2L);
        cart.setItem(item);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByItem_Id(1L)).thenReturn(Optional.of(cart));

        ItemDto itemDto = itemService.findItemById(1L);
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getTitle()).isEqualTo("Title 1");
        assertThat(itemDto.getDescription()).isEqualTo("Description 1");
        assertThat(itemDto.getImgPath()).isEqualTo("ImagePath 1");
        assertThat(itemDto.getCount()).isEqualTo(2L);
        assertThat(itemDto.getPrice()).isEqualTo(1337L);

        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemRepository);
        verify(cartRepository, times(1)).findByItem_Id(1L);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    void findItemById_whenCartNotPresent() {
        Item item = createItem();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByItem_Id(1L)).thenReturn(Optional.empty());

        ItemDto itemDto = itemService.findItemById(1L);
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getTitle()).isEqualTo("Title 1");
        assertThat(itemDto.getDescription()).isEqualTo("Description 1");
        assertThat(itemDto.getImgPath()).isEqualTo("ImagePath 1");
        assertThat(itemDto.getCount()).isNull();
        assertThat(itemDto.getPrice()).isEqualTo(1337L);

        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemRepository);
        verify(cartRepository, times(1)).findByItem_Id(1L);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    void findItemById_whenItemNotFound_throwsItemNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findItemById(1L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Item with id 1 not found");

        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemRepository);
        verifyNoInteractions(cartRepository);
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

            when(itemRepository.findByTitleContainingOrDescriptionContainingAllIgnoreCase(any(), any(), any()))
                    .thenReturn(items);
            when(itemRepository.count()).thenReturn(20L);

            when(cartRepository.findByItem_Id(anyLong())).thenReturn(Optional.empty());

            PageableItemsDto pageableItemsDto = itemService.getPageableItems(search, sortOption, pageSize, pageNumber);

            PageableDto pageableDto = pageableItemsDto.getPageable();
            assertThat(pageableDto.hasNext()).isFalse();
            assertThat(pageableDto.hasPrevious()).isFalse();
            assertThat(pageableDto.pageNumber()).isEqualTo(pageNumber);
            assertThat(pageableDto.pageSize()).isEqualTo(pageSize);

            List<List<ItemDto>> itemDtos = pageableItemsDto.getItems();
            assertThat(itemDtos).hasSize(4);
            List<ItemDto> firstItemDto = itemDtos.getFirst();
            assertThat(firstItemDto)
                    .hasSize(5)
                    .isSortedAccordingTo(Comparator.comparingLong(ItemDto::getId));
            List<ItemDto> lastItemDto = itemDtos.getLast();
            assertThat(lastItemDto)
                    .hasSize(5)
                    .isSortedAccordingTo(Comparator.comparingLong(ItemDto::getId));
            ItemDto firstDto = firstItemDto.getFirst();
            assertThat(firstDto.getId()).isEqualTo(1L);
            assertThat(firstDto.getTitle()).isEqualTo("Title 1");
            assertThat(firstDto.getDescription()).isEqualTo("Description 1");
            assertThat(firstDto.getImgPath()).isEqualTo("ImagePath 1");
            assertThat(firstDto.getCount()).isNull();
            assertThat(firstDto.getPrice()).isEqualTo(1337L);

            ItemDto lastDto = lastItemDto.getLast();
            assertThat(lastDto.getId()).isEqualTo(20L);
            assertThat(lastDto.getTitle()).isEqualTo("Title 20");
            assertThat(lastDto.getDescription()).isEqualTo("Description 20");
            assertThat(lastDto.getImgPath()).isEqualTo("ImagePath 20");
            assertThat(lastDto.getCount()).isNull();
            assertThat(lastDto.getPrice()).isEqualTo(1337L * 20L);

            verify(itemRepository, times(1)).findByTitleContainingOrDescriptionContainingAllIgnoreCase(any(), any(), any());
            verify(itemRepository, times(1)).count();
            verifyNoMoreInteractions(itemRepository);

            verify(cartRepository, times(20)).findByItem_Id(anyLong());
            verifyNoMoreInteractions(cartRepository);
        }

        @Test
        void findAll() {
            String search = "";
            SortOption sortOption = SortOption.NO;
            int pageSize = 20;
            int pageNumber = 1;

            List<Item> items = createItems();

            Page<Item> mockPage = new PageImpl<>(items);

            PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.unsorted());
            when(itemRepository.findAll(pageable)).thenReturn(mockPage);
            when(itemRepository.count()).thenReturn(20L);

            when(cartRepository.findByItem_Id(anyLong())).thenReturn(Optional.empty());

            PageableItemsDto pageableItemsDto = itemService.getPageableItems(search, sortOption, pageSize, pageNumber);

            PageableDto pageableDto = pageableItemsDto.getPageable();
            assertThat(pageableDto.hasNext()).isFalse();
            assertThat(pageableDto.hasPrevious()).isFalse();
            assertThat(pageableDto.pageNumber()).isEqualTo(pageNumber);
            assertThat(pageableDto.pageSize()).isEqualTo(pageSize);

            List<List<ItemDto>> itemDtos = pageableItemsDto.getItems();
            assertThat(itemDtos).hasSize(4);
            List<ItemDto> firstItemDto = itemDtos.getFirst();
            assertThat(firstItemDto)
                    .hasSize(5)
                    .isSortedAccordingTo(Comparator.comparingLong(ItemDto::getId));
            List<ItemDto> lastItemDto = itemDtos.getLast();
            assertThat(lastItemDto)
                    .hasSize(5)
                    .isSortedAccordingTo(Comparator.comparingLong(ItemDto::getId));
            ItemDto firstDto = firstItemDto.getFirst();
            assertThat(firstDto.getId()).isEqualTo(1L);
            assertThat(firstDto.getTitle()).isEqualTo("Title 1");
            assertThat(firstDto.getDescription()).isEqualTo("Description 1");
            assertThat(firstDto.getImgPath()).isEqualTo("ImagePath 1");
            assertThat(firstDto.getCount()).isNull();
            assertThat(firstDto.getPrice()).isEqualTo(1337L);

            ItemDto lastDto = lastItemDto.getLast();
            assertThat(lastDto.getId()).isEqualTo(20L);
            assertThat(lastDto.getTitle()).isEqualTo("Title 20");
            assertThat(lastDto.getDescription()).isEqualTo("Description 20");
            assertThat(lastDto.getImgPath()).isEqualTo("ImagePath 20");
            assertThat(lastDto.getCount()).isNull();
            assertThat(lastDto.getPrice()).isEqualTo(1337L * 20L);

            verify(itemRepository, times(1)).findAll(pageable);
            verify(itemRepository, times(1)).count();
            verifyNoMoreInteractions(itemRepository);

            verify(cartRepository, times(20)).findByItem_Id(anyLong());
            verifyNoMoreInteractions(cartRepository);
        }

        private List<Item> createItems() {
            List<Item> items = new ArrayList<>();
            for (long i = 1; i <= 20L; i++) {
                items.add(createItem(i));
            }

            return items;
        }

        private Item createItem(long i) {
            Item item = new Item();

            item.setId(i);
            item.setTitle("Title " + i);
            item.setDescription("Description " + i);
            item.setImagePath("ImagePath " + i);
            item.setPrice(1337L * i);

            return item;
        }
    }

    private Item createItem() {
        Item item = new Item();

        item.setId(1L);
        item.setTitle("Title 1");
        item.setDescription("Description 1");
        item.setImagePath("ImagePath 1");
        item.setPrice(1337L);

        return item;
    }
}