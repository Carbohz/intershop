package ru.carbohz.shop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.carbohz.shop.model.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
