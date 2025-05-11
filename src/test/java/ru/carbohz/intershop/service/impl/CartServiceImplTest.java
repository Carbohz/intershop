package ru.carbohz.intershop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        CartServiceImpl.class,
        ItemMapper.class,
})
class CartServiceImplTest {

    @Autowired
    CartServiceImpl cartService;

    @MockitoBean
    ItemRepository itemRepository;

    @MockitoBean
    OrderRepository orderRepository;

    @MockitoBean
    CartRepository cartRepository;

    @Test
    void getCartItems_whenCartIsPresent() {
        List<Cart> carts = new ArrayList<>();
        Cart cart1 = new Cart();
        cart1.setId(1L);
        cart1.setCount(10L);
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Item 1");
        item.setDescription("Description 1");
        item.setImagePath("ImagePath 1");
        item.setPrice(12345L);
        cart1.setItem(item);
        carts.add(cart1);

        Cart cart2 = new Cart();
        cart2.setId(2L);
        cart2.setCount(1L);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Item 2");
        item2.setDescription("Description 2");
        item2.setImagePath("ImagePath 2");
        item2.setPrice(10L);
        cart2.setItem(item2);
        carts.add(cart2);

        when(cartRepository.findAll()).thenReturn(carts);

        CartItemsDto cartItemsDto = cartService.getCartItems();
        List<ItemDto> itemDtos = cartItemsDto.getItems();
        assertThat(itemDtos).hasSize(2);
        ItemDto itemDto1 = itemDtos.get(0);
        assertThat(itemDto1.getId()).isEqualTo(1L);
        assertThat(itemDto1.getTitle()).isEqualTo("Item 1");
        assertThat(itemDto1.getDescription()).isEqualTo("Description 1");
        assertThat(itemDto1.getImgPath()).isEqualTo("ImagePath 1");
        assertThat(itemDto1.getPrice()).isEqualTo(12345L);
        ItemDto itemDto2 = itemDtos.get(1);
        assertThat(itemDto2.getId()).isEqualTo(2L);
        assertThat(itemDto2.getTitle()).isEqualTo("Item 2");
        assertThat(itemDto2.getDescription()).isEqualTo("Description 2");
        assertThat(itemDto2.getImgPath()).isEqualTo("ImagePath 2");
        assertThat(itemDto2.getPrice()).isEqualTo(10L);
        assertThat(cartItemsDto.getTotal()).isEqualTo(123460L);
        assertThat(cartItemsDto.isEmpty()).isFalse();

        verify(cartRepository, times(1)).findAll();
        verifyNoMoreInteractions(cartRepository);

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void getCartItems_whenCartIsEmpty() {
        when(cartRepository.findAll()).thenReturn(new ArrayList<>());

        CartItemsDto cartItemsDto = cartService.getCartItems();
        assertThat(cartItemsDto.getItems()).isEmpty();
        assertThat(cartItemsDto.getTotal()).isEqualTo(0L);
        assertThat(cartItemsDto.isEmpty()).isTrue();

        verify(cartRepository, times(1)).findAll();
        verifyNoMoreInteractions(cartRepository);

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void changeItemsInCart() {
    }

    @Test
    void createOrder() {
    }
}