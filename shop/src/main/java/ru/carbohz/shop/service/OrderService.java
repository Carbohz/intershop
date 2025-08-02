package ru.carbohz.shop.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.OrderDto;

public interface OrderService {
    Flux<OrderDto> getOrders();
    Mono<OrderDto> getOrderById(long orderId);
}
