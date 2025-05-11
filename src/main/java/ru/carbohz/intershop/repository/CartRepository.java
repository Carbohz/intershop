package ru.carbohz.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByItem_Id(Long itemId);

    void deleteByItem_Id(Long itemId);

    @Modifying
    @Query("UPDATE Cart c SET c.count = c.count + 1 WHERE c.item.id = :itemId")
    void increaseCountForItem(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Cart c SET c.count = c.count - 1 WHERE c.item.id = :itemId")
    void decreaseCountForItem(@Param("itemId") Long itemId);

    Long item(Item item);
}
