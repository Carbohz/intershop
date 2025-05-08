package ru.carbohz.intershop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.carbohz.intershop.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByTitleContainingOrDescriptionContainingAllIgnoreCase(String title, String description, Pageable pageable);
}
