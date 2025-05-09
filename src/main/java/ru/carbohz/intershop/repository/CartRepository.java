package ru.carbohz.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.carbohz.intershop.model.Cart;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByItem_Id(Long itemId);

    @Modifying
    @Query("UPDATE Cart c SET c.count = c.count + 1 WHERE c.item.id = :itemId")
    void increaseCountForItem(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Cart c SET c.count = c.count - 1 WHERE c.item.id = :itemId")
    void decreaseCountForItem(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Cart c SET c.count = 0 WHERE c.item.id = :itemId")
    void resetCountForItem(@Param("itemId") Long itemId);

    List<Cart> findAllByCountIsGreaterThan(long count);

    default List<Cart> findAllByCountIsGreaterThanZero() {
        return findAllByCountIsGreaterThan(0L);
    }
}
