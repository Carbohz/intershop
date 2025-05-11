package ru.carbohz.intershop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.carbohz.intershop.TestcontainersConfiguration;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CartRepositoryTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void findByItem_Id() {
        Optional<Item> maybeItem = itemRepository.findById(1L);
        assertThat(maybeItem).isPresent();
        Item item = maybeItem.get();

        Cart cart = new Cart();
        cart.setItem(item);
        cart.setCount(10L);
        cartRepository.save(cart);

        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();
        assertThat(found.getItem().getId()).isEqualTo(1L);
        assertThat(found.getCount()).isEqualTo(10L);
    }

    @Test
    void increaseCountForItem() {
    }

    @Test
    void decreaseCountForItem() {
    }

    @Test
    void resetCountForItem() {
    }

    @Test
    void findAllByCountIsGreaterThan() {
    }

    @Test
    void findAllByCountIsGreaterThanZero() {
    }
}