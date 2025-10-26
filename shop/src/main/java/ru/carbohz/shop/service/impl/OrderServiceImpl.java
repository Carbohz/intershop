package ru.carbohz.shop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.OrderDto;
import ru.carbohz.shop.exception.OrderNotFoundException;
import ru.carbohz.shop.mapper.OrderMapper;
import ru.carbohz.shop.model.Order;
import ru.carbohz.shop.repository.OrderItemRepository;
import ru.carbohz.shop.repository.OrderRepository;
import ru.carbohz.shop.service.OrderService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    @Override
    public Flux<OrderDto> getOrders(final Long userId) {
        return orderRepository.findAllByUserId(userId)
                .flatMap(this::enrichOrderWithItems)
                .onErrorResume(e -> {
                    log.error("Error fetching orders: {}", e.getMessage());
                    return Flux.error(e);
                });
    }

    @Override
    public Mono<OrderDto> getOrderById(long orderId, Long userId) {
        log.info("Fetching order by ID: {}", orderId);
        return orderRepository.findByIdAndUserId(orderId, userId)
                .flatMap(this::enrichOrderWithItems)
                .switchIfEmpty(Mono.defer(() -> {
                    String message = "Order with id %d not found".formatted(orderId);
                    log.error(message);
                    return Mono.error(new OrderNotFoundException(message));
                }))
                .onErrorResume(e -> {
                    log.error("Error fetching order {}: {}", orderId, e.getMessage());
                    return Mono.error(e);
                });
    }

    private Mono<OrderDto> enrichOrderWithItems(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> orderMapper.toOrderDto(order, orderItems))
                .doOnSuccess(dto -> log.info("Successfully mapped order {}", order.getId()))
                .doOnError(e -> log.error("Error mapping order {}: {}", order.getId(), e.getMessage()));
    }
}
