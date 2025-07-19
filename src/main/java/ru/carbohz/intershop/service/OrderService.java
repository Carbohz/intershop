package ru.carbohz.intershop.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.OrderDto;

import java.util.List;

public interface OrderService {
    Flux<OrderDto> getOrders();
    Mono<OrderDto> getOrderById(long orderId);
}
