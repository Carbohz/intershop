package ru.carbohz.intershop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.exception.ItemNotFoundException;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.service.PageableService;

import java.util.Optional;

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

    @Test
    void getPageableItems() {
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