package ru.carbohz.intershop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.model.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("""
        SELECT * FROM items
        LIMIT :limit
        OFFSET :offset
        """)
    Flux<Item> findAll(@Param("limit") int limit, @Param("offset") long offset);

    @Query("""
        SELECT * FROM items
        WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))
        LIMIT :limit 
        OFFSET :offset
    """)
    Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") long offset);

    @Query("""
    SELECT COUNT(*) FROM items
    WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Mono<Long> countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("search") String search);
}
