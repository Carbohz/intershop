package ru.carbohz.intershop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.carbohz.intershop.model.OrderItem;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
    @Query("SELECT * FROM order_items oi WHERE oi.order_id = :orderId")
    Flux<OrderItem> findByOrderId(Long orderId);
}
