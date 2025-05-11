package ru.carbohz.intershop.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.mapper.CartMapper;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        CartServiceImpl.class,
        ItemMapper.class,
        CartMapper.class,
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
        List<Cart> carts = createCarts();

        when(cartRepository.findAll()).thenReturn(carts);

        CartItemsDto cartItemsDto = cartService.getCartItems();
        assertThat(cartItemsDto.isEmpty()).isFalse();
        assertThat(cartItemsDto.getTotal()).isEqualTo(123460L);

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

        verify(cartRepository, times(1)).findAll();
        verifyNoMoreInteractions(cartRepository);

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(orderRepository);
    }

    private static List<Cart> createCarts() {
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
        return carts;
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

    @Nested
    class ChangeItemsInCart {
        @Test
        void onActionPlus_whenCartIsEmpty() {
            Long itemId = 1L;
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Optional.empty());
            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
            when(cartRepository.save(any())).thenReturn(any());

            cartService.changeItemsInCart(itemId, Action.PLUS);

            verify(cartRepository, times(1)).findByItem_Id(itemId);
            verify(cartRepository, times(1)).save(any());
            verifyNoMoreInteractions(cartRepository);

            verify(itemRepository, times(1)).findById(itemId);
            verifyNoMoreInteractions(itemRepository);

            verifyNoInteractions(orderRepository);
        }

        @Test
        void onActionPlus_whenCartIsPresent() {
            Long itemId = 1L;
            Cart cart = new Cart();
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Optional.of(cart));
            doNothing().when(cartRepository).increaseCountForItem(itemId);

            cartService.changeItemsInCart(itemId, Action.PLUS);

            verify(cartRepository, times(1)).findByItem_Id(itemId);
            verify(cartRepository, times(1)).increaseCountForItem(itemId);
            verifyNoMoreInteractions(cartRepository);

            verifyNoInteractions(itemRepository);
            verifyNoInteractions(orderRepository);
        }

        @Test
        void onActionMinus_whenCartIsEmpty() {
            Long itemId = 1L;
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Optional.empty());

            cartService.changeItemsInCart(itemId, Action.MINUS);

            verify(cartRepository, times(1)).findByItem_Id(itemId);
            verifyNoMoreInteractions(cartRepository);

            verifyNoInteractions(itemRepository);
            verifyNoInteractions(orderRepository);
        }

        @Test
        void onActionMinus_whenCartIsPresent_andCountIsOne() {
            Long itemId = 1L;
            Cart cart = new Cart();
            cart.setCount(1L);
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Optional.of(cart));
            doNothing().when(cartRepository).deleteByItem_Id(itemId);

            cartService.changeItemsInCart(itemId, Action.MINUS);

            verify(cartRepository, times(1)).findByItem_Id(itemId);
            verify(cartRepository, times(1)).deleteByItem_Id(itemId);
            verifyNoMoreInteractions(cartRepository);

            verifyNoInteractions(itemRepository);
            verifyNoInteractions(orderRepository);
        }

        @Test
        void onActionMinus_whenCartIsPresent_andCountIsMoreThanOne() {
            Long itemId = 1L;
            Cart cart = new Cart();
            cart.setCount(10L);
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Optional.of(cart));
            doNothing().when(cartRepository).decreaseCountForItem(itemId);

            cartService.changeItemsInCart(itemId, Action.MINUS);

            verify(cartRepository, times(1)).findByItem_Id(itemId);
            verify(cartRepository, times(1)).decreaseCountForItem(itemId);
            verifyNoMoreInteractions(cartRepository);

            verifyNoInteractions(itemRepository);
            verifyNoInteractions(orderRepository);
        }

        @Test
        void onActionDelete() {
            Long itemId = 1L;
            doNothing().when(cartRepository).deleteByItem_Id(itemId);

            cartService.changeItemsInCart(itemId, Action.DELETE);

            verify(cartRepository, times(1)).deleteByItem_Id(itemId);
            verifyNoMoreInteractions(cartRepository);

            verifyNoInteractions(itemRepository);
            verifyNoInteractions(orderRepository);
        }
    }

    @Test
    void createOrder() {
        List<Cart> carts = createCarts();
        when(cartRepository.findAll()).thenReturn(carts);
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.save(any())).thenReturn(order);

        Long createdId = cartService.createOrder();
        assertThat(createdId).isEqualTo(1L);

        verify(cartRepository, times(1)).findAll();
        verify(cartRepository, times(1)).deleteAll();
        verifyNoMoreInteractions(cartRepository);

        verify(orderRepository, times(1)).save(any());
        verifyNoMoreInteractions(orderRepository);

        verifyNoInteractions(itemRepository);
    }
}