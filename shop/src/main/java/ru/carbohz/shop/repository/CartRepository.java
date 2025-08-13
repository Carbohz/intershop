package ru.carbohz.shop.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.Cart;

public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
    @Query("SELECT * FROM carts c LEFT JOIN items i ON i.id=c.item_id WHERE i.id = :itemId")
    Mono<Cart> findByItem_Id(Long itemId);

    default Mono<Void> deleteByItem_Id(Long itemId) {
        return findByItem_Id(itemId)
                .flatMap(this::delete)  // Delete each item
                .then();
    }

    @Modifying
    @Query("UPDATE carts SET count = count + 1 WHERE item_id = :itemId")
    Mono<Integer> increaseCountForItem(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE carts SET count = count - 1 WHERE item_id = :itemId")
    Mono<Integer> decreaseCountForItem(@Param("itemId") Long itemId);
}
