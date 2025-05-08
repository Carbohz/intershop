package ru.carbohz.intershop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.carbohz.intershop.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByTitleContainingOrDescriptionContainingAllIgnoreCase(String title, String description, Pageable pageable);

    @Modifying
    @Query("UPDATE Item i SET i.count = i.count + 1 WHERE i.id = :id")
    void increaseCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Item i SET i.count = i.count - 1 WHERE i.id = :id")
    void decreaseCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Item i SET i.count = 0 WHERE i.id = :id")
    void resetCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Item i SET i.count = 0")
    void resetCountForAll();

    List<Item> findAllByCountIsGreaterThan(long count);
}
