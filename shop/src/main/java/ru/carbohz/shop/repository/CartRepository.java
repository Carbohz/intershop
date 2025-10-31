package ru.carbohz.shop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.Cart;

public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
    @Query("SELECT * FROM carts c WHERE c.user_id = :userId")
    Flux<Cart> findAllByUserId(Long userId);

    @Query("SELECT * FROM carts c LEFT JOIN items i ON i.id=c.item_id WHERE i.id = :itemId")
    Mono<Cart> findByItem_Id(Long itemId);

    @Query("SELECT * FROM carts c WHERE c.item_id = :itemId AND c.user_id = :userId")
    Mono<Cart> findByItemIdAndUserId(Long itemId, Long userId);

    default Mono<Void> deleteByItem_Id(Long itemId, Long userId) {
        return findByItemIdAndUserId(itemId, userId)
                .flatMap(this::delete)  // Delete each item
                .then();
    }

    Mono<Void> deleteAllByUserId(Long userId);
}
