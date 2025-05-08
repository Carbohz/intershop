package ru.carbohz.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.carbohz.intershop.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
