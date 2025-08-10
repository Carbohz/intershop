package ru.carbohz.shop.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.shop.api.PaymentApi;
import ru.carbohz.shop.dto.CartItemsDto;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.mapper.CartMapper;
import ru.carbohz.shop.mapper.ItemMapper;
import ru.carbohz.shop.model.Action;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.Order;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;
import ru.carbohz.shop.repository.OrderItemRepository;
import ru.carbohz.shop.repository.OrderRepository;
import ru.carbohz.shop.service.ItemService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        CartServiceImpl.class,
        CartMapper.class,
        ItemMapper.class,
})
class CartServiceImplTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private PaymentApi paymentApi;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private CartServiceImpl cartService;

    @Test
    void getCartItems_whenCartIsPresent() {
        // Setup test data
        List<Cart> carts = createCarts();

        // Mock repository responses
        when(cartRepository.findAll()).thenReturn(Flux.fromIterable(carts));
        when(itemService.findItemById(1L)).thenReturn(
                Mono.just(new ItemDto(1L, "Item 1", "Description 1", "ImagePath 1", 10L, 12345L)));
        when(itemService.findItemById(2L)).thenReturn(
                Mono.just(new ItemDto(2L, "Item 2", "Description 2", "ImagePath 2", 1L, 10L)));

        // Execute and verify
        Mono<CartItemsDto> result = cartService.getCartItems();

        StepVerifier.create(result)
                .assertNext(cartItemsDto -> {
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
                })
                .verifyComplete();


        verifyNoInteractions(itemRepository);
        verifyNoInteractions(orderRepository);
        verify(cartRepository, times(1)).findAll();
        verifyNoInteractions(orderItemRepository);
        verifyNoInteractions(paymentApi);
        verify(itemService, times(1)).findItemById(1L);
        verify(itemService, times(1)).findItemById(2L);
        verifyNoMoreInteractions(cartRepository, itemService);
    }

    @Test
    void getCartItems_whenCartIsEmpty() {
        // Mock repository responses
        when(cartRepository.findAll()).thenReturn(Flux.empty());

        // Execute and verify
        Mono<CartItemsDto> result = cartService.getCartItems();

        StepVerifier.create(result)
                .assertNext(cartItemsDto -> {
                    assertThat(cartItemsDto.getItems()).isEmpty();
                    assertThat(cartItemsDto.getTotal()).isEqualTo(0L);
                    assertThat(cartItemsDto.isEmpty()).isTrue();
                })
                .verifyComplete();

        verify(cartRepository).findAll();
//        verifyNoInteractions(itemRepository, cartMapper);
        verifyNoMoreInteractions(cartRepository);
    }

    @Nested
    class ChangeItemsInCart {
        @Test
        void onActionPlus_whenCartIsEmpty() {
            Long itemId = 1L;
            Item item = createItem(itemId, "Test Item", "Desc", "img", 100L);

            // Mock repository responses
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Mono.empty());
            when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));
            when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(new Cart()));

            // Execute and verify
            Mono<Void> result = cartService.changeItemsInCart(itemId, Action.PLUS);

            StepVerifier.create(result).verifyComplete();

            verify(cartRepository).findByItem_Id(itemId);
            verify(itemRepository).findById(itemId);
            verify(cartRepository).save(any(Cart.class));
            verifyNoMoreInteractions(cartRepository, itemRepository);
        }

        @Test
        void onActionPlus_whenCartIsPresent() {
            Long itemId = 1L;
            Cart cart = new Cart();
            cart.setId(1L);
            cart.setItemId(itemId);
            cart.setCount(5L);

            // Mock repository responses
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Mono.just(cart));
            when(cartRepository.save(cart)).thenReturn(Mono.just(cart));
            when(itemRepository.findById(itemId)).thenReturn(Mono.just(createItem(itemId, "Test Item", "Desc", "img", 100L)));

            // Execute and verify
            Mono<Void> result = cartService.changeItemsInCart(itemId, Action.PLUS);

            StepVerifier.create(result).verifyComplete();

            verify(cartRepository).findByItem_Id(itemId);
            verify(cartRepository).save(cart);
            assertThat(cart.getCount()).isEqualTo(6L);
            verify(itemRepository, times(1)).findById(itemId);
            verifyNoMoreInteractions(itemRepository);
            verifyNoMoreInteractions(cartRepository);
        }

        @Test
        void onActionMinus_whenCartIsEmpty() {
            Long itemId = 1L;

            // Mock repository responses
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Mono.empty());

            // Execute and verify
            Mono<Void> result = cartService.changeItemsInCart(itemId, Action.MINUS);

            StepVerifier.create(result).verifyComplete();

            verify(cartRepository).findByItem_Id(itemId);
            verifyNoMoreInteractions(cartRepository);
            verifyNoInteractions(itemRepository);
        }

        @Test
        void onActionMinus_whenCartIsPresent_andCountIsOne() {
            Long itemId = 1L;
            Cart cart = new Cart();
            cart.setId(1L);
            cart.setItemId(itemId);
            cart.setCount(1L);

            // Mock repository responses
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Mono.just(cart));
            when(cartRepository.deleteById(cart.getId())).thenReturn(Mono.empty());

            // Execute and verify
            Mono<Void> result = cartService.changeItemsInCart(itemId, Action.MINUS);

            StepVerifier.create(result).verifyComplete();

            verify(cartRepository).findByItem_Id(itemId);
            verify(cartRepository).deleteById(cart.getId());
            verifyNoMoreInteractions(cartRepository);
            verifyNoInteractions(itemRepository);
        }

        @Test
        void onActionMinus_whenCartIsPresent_andCountIsMoreThanOne() {
            Long itemId = 1L;
            Cart cart = new Cart();
            cart.setId(1L);
            cart.setItemId(itemId);
            cart.setCount(10L);

            // Mock repository responses
            when(cartRepository.findByItem_Id(itemId)).thenReturn(Mono.just(cart));
            when(cartRepository.save(cart)).thenReturn(Mono.just(cart));

            // Execute and verify
            Mono<Void> result = cartService.changeItemsInCart(itemId, Action.MINUS);

            StepVerifier.create(result).verifyComplete();

            verify(cartRepository).findByItem_Id(itemId);
            verify(cartRepository).save(cart);
            assertThat(cart.getCount()).isEqualTo(9L);
            verifyNoMoreInteractions(cartRepository);
            verifyNoInteractions(itemRepository);
        }

        @Test
        void onActionDelete() {
            Long itemId = 1L;

            // Mock repository responses
            when(cartRepository.deleteByItem_Id(itemId)).thenReturn(Mono.empty());

            // Execute and verify
            Mono<Void> result = cartService.changeItemsInCart(itemId, Action.DELETE);

            StepVerifier.create(result).verifyComplete();

            verify(cartRepository).deleteByItem_Id(itemId);
            verifyNoMoreInteractions(cartRepository);
            verifyNoInteractions(itemRepository);
        }
    }

    @Test
    void createOrder() {
        // Setup test data
        List<Cart> carts = createCarts();
        Map<Long, Item> items = Map.of(
                1L, createItem(1L, "Item 1", "Desc 1", "img1", 12345L),
                2L, createItem(2L, "Item 2", "Desc 2", "img2", 10L)
        );

        Order order = new Order();
        order.setId(1L);
        order.setTotalSum(123460L);

        // Mock repository responses
        when(cartRepository.findAll()).thenReturn(Flux.fromIterable(carts));
        when(orderRepository.save(any())).thenReturn(Mono.just(order));
        when(itemService.findItemById(1L)).thenReturn(Mono.just(new ItemDto(1L, "Item 1", "Desc 1", "img1", 10L, 12345L)));
        when(itemService.findItemById(2L)).thenReturn(Mono.just(new ItemDto(2L, "Item 2", "Desc 2", "img2", 1L, 10L)));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.empty());
        when(cartRepository.deleteAll()).thenReturn(Mono.empty());
        when(paymentApi.balancePostWithHttpInfo(any())).thenReturn(
                Mono.just(new ResponseEntity<>(HttpStatus.OK)));

        // Execute and verify
        Mono<Long> result = cartService.createOrder();

        StepVerifier.create(result)
                .assertNext(orderId -> assertThat(orderId).isEqualTo(1L))
                .verifyComplete();

        verify(cartRepository).findAll();
        verifyNoInteractions(itemRepository);
        verify(orderRepository).save(argThat(actual -> Objects.equals(actual.getTotalSum(), order.getTotalSum())));
        verify(cartRepository).deleteAll();
        verify(paymentApi, times(1)).balancePostWithHttpInfo(any());
        verifyNoMoreInteractions(cartRepository, orderRepository, paymentApi);
    }

    @Test
    void createOrder_whenNotEnoughBalance_shouldThrowException() {
        // Setup test data
        List<Cart> carts = createCarts();

        Order order = new Order();
        order.setId(1L);
        order.setTotalSum(123460L);

        // Mock repository responses
        when(cartRepository.findAll()).thenReturn(Flux.fromIterable(carts));
        when(itemService.findItemById(1L)).thenReturn(Mono.just(new ItemDto(1L, "Item 1", "Desc 1", "img1", 10L, 12345L)));
        when(itemService.findItemById(2L)).thenReturn(Mono.just(new ItemDto(2L, "Item 2", "Desc 2", "img2", 1L, 10L)));
        when(paymentApi.balancePostWithHttpInfo(any())).thenReturn(Mono.error(new RuntimeException()));

        // Execute and verify
        Mono<Long> result = cartService.createOrder();

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(orderRepository);
        verify(cartRepository).findAll();
        verifyNoInteractions(orderItemRepository);
        verify(paymentApi, times(1)).balancePostWithHttpInfo(any());
        verify(itemService, times(1)).findItemById(1L);
        verify(itemService, times(1)).findItemById(2L);
        verifyNoMoreInteractions(cartRepository, paymentApi, itemService);
    }

    private List<Cart> createCarts() {
        Cart cart1 = new Cart();
        cart1.setId(1L);
        cart1.setItemId(1L);
        cart1.setCount(10L);

        Cart cart2 = new Cart();
        cart2.setId(2L);
        cart2.setItemId(2L);
        cart2.setCount(1L);

        return List.of(cart1, cart2);
    }

    private Item createItem(Long id, String title, String description, String imagePath, Long price) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setImagePath(imagePath);
        item.setPrice(price);
        return item;
    }

    private CartItemsDto createCartItemsDto() {
        return new CartItemsDto(
                List.of(
                        new ItemDto(1L, "Item 1", "Description 1", "ImagePath 1", 10L, 12345L),
                        new ItemDto(2L, "Item 2", "Description 2", "ImagePath 2", 1L, 10L)
                ),
                123460L,
                false
        );
    }
}