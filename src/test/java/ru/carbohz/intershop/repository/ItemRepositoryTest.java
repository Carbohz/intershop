package ru.carbohz.intershop.repository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.TestcontainersConfiguration;
import ru.carbohz.intershop.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    void findAll() {
        int limit = 10;
        int offset = 10;
        Iterable<Item> items = itemRepository.findAll(limit, offset).toIterable();

        assertThat(items).hasSize(limit);
        assertThat(items).extracting(Item::getId).allSatisfy(id -> {
            assertThat(id).isGreaterThan(limit);
            assertThat(id).isLessThanOrEqualTo(limit + offset);
        });
    }

    @Nested
    class ByTitleContainingOrDescriptionContainingAllIgnoreCase {
        @Test
        void find() {
            String search = "lApToP";
            String searchNorm = search.toLowerCase();
            long offset = 0;
            long limit = itemRepository.count().block();
            Iterable<Item> items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search, (int) limit, offset).toIterable();
            assertThat(items).hasSize(2);
            assertThat(items).allSatisfy(item -> {
                assertThat(item.getTitle().contains(searchNorm) ||
                           item.getDescription().contains(searchNorm)).isTrue();
            });
        }

        @Test
        void count() {
            String search = "lApToP";
            Mono<Long> count = itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search);
            assertThat(count.block()).isEqualTo(2L);
        }
    }

}