package ru.carbohz.shop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAllByUserId(Long userId);
    Mono<Order> findByIdAndUserId(Long id, Long userId);
}
