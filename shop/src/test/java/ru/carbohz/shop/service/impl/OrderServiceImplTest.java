package ru.carbohz.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.dto.OrderDto;
import ru.carbohz.shop.exception.OrderNotFoundException;
import ru.carbohz.shop.mapper.OrderMapper;
import ru.carbohz.shop.model.Order;
import ru.carbohz.shop.model.OrderItem;
import ru.carbohz.shop.repository.OrderItemRepository;
import ru.carbohz.shop.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getOrders() {
        // Create test orders (without items - they're in separate table)
        Long userId = 1337L;
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(userId);
        order1.setTotalSum(100500L);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(userId);
        order2.setTotalSum(837L);

        // Create test order items
        OrderItem order1Item1 = createOrderItem(1L, 10L, userId, "Item 1", 100000L, 1L, 1L);
        OrderItem order1Item2 = createOrderItem(2L, 14L, userId, "Item 2", 250L, 2L, 1L);
        OrderItem order2Item1 = createOrderItem(3L, 1440L, userId, "Item 3", 837L, 1L, 2L);

        // Mock repository responses
        when(orderRepository.findAllByUserId(userId)).thenReturn(Flux.just(order1, order2));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(order1Item1, order1Item2));
        when(orderItemRepository.findByOrderId(2L)).thenReturn(Flux.just(order2Item1));

        // Create expected DTOs
        OrderDto orderDto1 = createOrderDto(1L, 100500L,
                List.of(
                        createItemDto(10L, "Item 1", 100000L, 1L),
                        createItemDto(14L, "Item 2", 250L, 2L)
                )
        );

        OrderDto orderDto2 = createOrderDto(2L, 837L,
                List.of(
                        createItemDto(1440L, "Item 3", 837L, 1L)
                )
        );

        // Mock mapper responses
        when(orderMapper.toOrderDto(order1, List.of(order1Item1, order1Item2)))
                .thenReturn(Mono.just(orderDto1));
        when(orderMapper.toOrderDto(order2, List.of(order2Item1)))
                .thenReturn(Mono.just(orderDto2));

        // Execute and verify
        Flux<OrderDto> result = orderService.getOrders(userId);

        StepVerifier.create(result.collectList())
                .assertNext(orderDtos -> {
                    assertThat(orderDtos).hasSize(2);

                    // Verify first order
                    OrderDto dto1 = orderDtos.get(0);
                    assertThat(dto1.id()).isEqualTo(1L);
                    assertThat(dto1.totalSum()).isEqualTo(100500L);
                    assertThat(dto1.items()).hasSize(2);

                    // Verify second order
                    OrderDto dto2 = orderDtos.get(1);
                    assertThat(dto2.id()).isEqualTo(2L);
                    assertThat(dto2.totalSum()).isEqualTo(837L);
                    assertThat(dto2.items()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    void getOrderById() {
        // Create test order
        Long userId = 1337L;
        Order order = new Order();
        order.setUserId(userId);
        order.setId(1L);
        order.setTotalSum(100500L);

        // Create test order items
        OrderItem item1 = createOrderItem(1L, 10L, userId, "Item 1", 100000L, 1L, 1L);
        OrderItem item2 = createOrderItem(2L, 14L, userId, "Item 2", 250L, 2L, 1L);

        // Mock repository responses
        when(orderRepository.findByIdAndUserId(1L, userId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(item1, item2));

        // Create expected DTO
        OrderDto expectedDto = createOrderDto(1L, 100500L,
                List.of(
                        createItemDto(10L, "Item 1", 100000L, 1L),
                        createItemDto(14L, "Item 2", 250L, 2L)
                )
        );

        // Mock mapper response
        when(orderMapper.toOrderDto(order, List.of(item1, item2)))
                .thenReturn(Mono.just(expectedDto));

        // Execute and verify
        Mono<OrderDto> result = orderService.getOrderById(1L, userId);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto.id()).isEqualTo(1L);
                    assertThat(dto.totalSum()).isEqualTo(100500L);
                    assertThat(dto.items()).hasSize(2);

                    // Verify first item
                    ItemDto itemDto1 = dto.items().get(0);
                    assertThat(itemDto1.getId()).isEqualTo(10L);
                    assertThat(itemDto1.getTitle()).isEqualTo("Item 1");
                    assertThat(itemDto1.getPrice()).isEqualTo(100000L);
                    assertThat(itemDto1.getCount()).isEqualTo(1L);

                    // Verify second item
                    ItemDto itemDto2 = dto.items().get(1);
                    assertThat(itemDto2.getId()).isEqualTo(14L);
                    assertThat(itemDto2.getTitle()).isEqualTo("Item 2");
                    assertThat(itemDto2.getPrice()).isEqualTo(250L);
                    assertThat(itemDto2.getCount()).isEqualTo(2L);
                })
                .verifyComplete();
    }

    @Test
    void getOrderById_whenOrderNotFound_throwsOrderNotFoundException() {
        Long orderId = 1L;
        Long userId = 1337L;
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Mono.empty());

        Mono<OrderDto> result = orderService.getOrderById(orderId, userId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof OrderNotFoundException &&
                        throwable.getMessage().equals("Order with id 1 not found"))
                .verify();
    }

    private OrderItem createOrderItem(Long id, Long itemId, Long userId, String title, Long price, Long count, Long orderId) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setItemId(itemId);
        item.setUserId(userId);
        item.setTitle(title);
        item.setPrice(price);
        item.setCount(count);
        item.setOrderId(orderId);
        return item;
    }

    private OrderDto createOrderDto(Long id, Long totalSum, List<ItemDto> items) {
        return new OrderDto(id, items, totalSum);
    }

    private ItemDto createItemDto(Long id, String title, Long price, Long count) {
        return new ItemDto(id, title, null, null, count, price);
    }
}