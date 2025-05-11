package ru.carbohz.intershop.repository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import ru.carbohz.intershop.TestcontainersConfiguration;
import ru.carbohz.intershop.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Nested
    class FindByTitleContainingOrDescriptionContainingAllIgnoreCase {
        @Test
        void findByTitleIgnoreCaseOrDescriptionAllIgnoreCase() {
            String search = "lApToP";
            long count = itemRepository.count();
            PageRequest pageRequest = PageRequest.of(0, (int) count);
            List<Item> items = itemRepository.findByTitleContainingOrDescriptionContainingAllIgnoreCase(search, search, pageRequest);
            assertThat(items).hasSize(2);
            assertThat(items.get(0).getTitle()).containsIgnoringCase(search);
            assertThat(items.get(1).getDescription()).containsIgnoringCase(search);
        }

        @Test
        void notFound() {
            String search = "brainrot";
            long count = itemRepository.count();
            PageRequest pageRequest = PageRequest.of(0, (int) count);
            List<Item> items = itemRepository.findByTitleContainingOrDescriptionContainingAllIgnoreCase(search, search, pageRequest);
            assertThat(items).isEmpty();
        }
    }

}