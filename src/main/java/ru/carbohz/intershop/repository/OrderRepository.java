package ru.carbohz.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.carbohz.intershop.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
