package ru.carbohz.shop.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;
import ru.carbohz.shop.TestcontainersConfiguration;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CartRepositoryTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        Optional<Item> maybeItem = itemRepository.findById(1L).blockOptional();
        assertThat(maybeItem).isPresent();
        Item item = maybeItem.get();

        Cart cart = new Cart();
        cart.setItemId(item.getId());
        cart.setCount(10L);
        cartRepository.save(cart).block();
    }

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll().block();
    }

    @Test
    void findByItem_Id() {
        StepVerifier.create(cartRepository.findByItem_Id(1L))
                .assertNext(cart -> {
                    assertThat(cart.getItemId()).isEqualTo(1L);
                    assertThat(cart.getCount()).isEqualTo(10L);
                })
                .verifyComplete();
    }

    @Test
    void increaseCountForItem() {
        // When
        StepVerifier.create(cartRepository.increaseCountForItem(1L))
                .assertNext(updatedRows -> assertThat(updatedRows).isEqualTo(1))
                .verifyComplete();

        // Then
        StepVerifier.create(cartRepository.findByItem_Id(1L))
                .assertNext(cart -> assertThat(cart.getCount()).isEqualTo(11L))
                .verifyComplete();
    }

    @Test
    void decreaseCountForItem() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L).blockOptional();
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();

        cartRepository.decreaseCountForItem(found.getItemId()).block();

        Optional<Cart> maybeCart2 = cartRepository.findByItem_Id(1L).blockOptional();
        assertThat(maybeCart2).isPresent();
        Cart found2 = maybeCart2.get();
        assertThat(found2.getCount()).isEqualTo(found.getCount() - 1);
    }

    @Test
    void deleteByItem_Id() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L).blockOptional();
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();

        cartRepository.deleteByItem_Id(found.getItemId()).block();

        Optional<Cart> maybeCart2 = cartRepository.findByItem_Id(1L).blockOptional();
        assertThat(maybeCart2).isEmpty();
    }
}