package ru.carbohz.shop.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;
import ru.carbohz.shop.config.NoCacheTestConfiguration;
import ru.carbohz.shop.config.PostgresTestcontainersConfiguration;
import ru.carbohz.shop.model.Cart;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTestcontainersConfiguration.class, NoCacheTestConfiguration.class})
class CartRepositoryTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("admin");
        user.setPassword("admin");
        User saved = userRepository.save(user).block();
        Long userId = saved.getId();

        Optional<Item> maybeItem = itemRepository.findById(1L).blockOptional();
        assertThat(maybeItem).isPresent();
        Item item = maybeItem.get();

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItemId(item.getId());
        cart.setCount(10L);
        cartRepository.save(cart).block();
    }

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll().block();
        userRepository.deleteAll().block();
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
    void deleteByItem_Id() {
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(1L).blockOptional();
        assertThat(maybeCart).isPresent();
        Cart found = maybeCart.get();

        cartRepository.deleteByItem_Id(found.getItemId(), found.getUserId()).block();

        Optional<Cart> maybeCart2 = cartRepository.findByItem_Id(1L).blockOptional();
        assertThat(maybeCart2).isEmpty();
    }
}