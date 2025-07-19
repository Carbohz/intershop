package ru.carbohz.intershop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.carbohz.intershop.model.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
