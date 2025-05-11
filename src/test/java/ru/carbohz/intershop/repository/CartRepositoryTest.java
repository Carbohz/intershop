package ru.carbohz.intershop.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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

    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Optional<Item> maybeItem = itemRepository.findById(1L);
        assertThat(maybeItem).isPresent();
        Item item = maybeItem.get();

        Cart cart = new Cart();
        cart.setItem(item);
        cart.setCount(10L);
        cartRepository.save(cart);
    }

    @Test
    void findByItem_Id() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();
        assertThat(found.getItem().getId()).isEqualTo(1L);
        assertThat(found.getCount()).isEqualTo(10L);
    }

    @Test
    void increaseCountForItem() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();

        cartRepository.increaseCountForItem(found.getItem().getId());
        entityManager.clear();

        Optional<Cart> maybeCart2 = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart2).isPresent();
        Cart found2 = maybeCart2.get();
        assertThat(found2.getCount()).isEqualTo(found.getCount() + 1);
    }

    @Test
    void decreaseCountForItem() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();

        cartRepository.decreaseCountForItem(found.getItem().getId());
        entityManager.clear();

        Optional<Cart> maybeCart2 = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart2).isPresent();
        Cart found2 = maybeCart2.get();
        assertThat(found2.getCount()).isEqualTo(found.getCount() - 1);
    }

    @Test
    void deleteByItem_Id() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();

        cartRepository.deleteByItem_Id(found.getItem().getId());

        Optional<Cart> maybeCart2 = cartRepository.findByItem_Id(1L);
        assertThat(maybeCart2).isEmpty();
    }
}