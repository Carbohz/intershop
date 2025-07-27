package ru.carbohz.intershop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.exception.OrderNotFoundException;
import ru.carbohz.intershop.mapper.OrderMapper;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.OrderItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;
import ru.carbohz.intershop.service.OrderService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    @Override
    public Flux<OrderDto> getOrders() {
        log.trace("Returning orders reactively");
        return orderRepository.findAll()
                .flatMap(this::enrichOrderWithItems)
                .onErrorResume(e -> {
                    log.error("Error fetching orders: {}", e.getMessage());
                    return Flux.error(e);
                });
    }

    @Override
    public Mono<OrderDto> getOrderById(long orderId) {
        log.trace("Fetching order by ID: {}", orderId);
        return orderRepository.findById(orderId)
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
