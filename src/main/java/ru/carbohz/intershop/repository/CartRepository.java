package ru.carbohz.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.carbohz.intershop.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
