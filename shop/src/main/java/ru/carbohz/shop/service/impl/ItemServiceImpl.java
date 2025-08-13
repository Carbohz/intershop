package ru.carbohz.shop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.dto.PageableDto;
import ru.carbohz.shop.dto.PageableItemsDto;
import ru.carbohz.shop.exception.ItemNotFoundException;
import ru.carbohz.shop.mapper.ItemMapper;
import ru.carbohz.shop.model.Item;
import ru.carbohz.shop.model.SortOption;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;
import ru.carbohz.shop.service.ItemService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CartRepository cartRepository;

    @Override
    @Cacheable(value = "item", key = "#id")
    public Mono<ItemDto> findItemById(Long id) {
        return itemRepository.findById(id)
                .flatMap(this::toItemDto)
                .switchIfEmpty(Mono.defer(() -> {
                    String message = "Item with id %d not found".formatted(id);
                    log.error(message);
                    return Mono.error(new ItemNotFoundException(message));
                }))
                .onErrorResume(e -> {
                    log.error("Error finding item {}: {}", id, e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    @Cacheable(value = "items", key = "{#search, #sort, #pageSize, #pageNumber}")
    public Mono<PageableItemsDto> getPageableItems(String search, SortOption sort, int pageSize, int pageNumber) {
        Comparator<ItemDto> order = sort(sort);

        return (search.isBlank() ? itemRepository.count() :
                itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search))
                .flatMap(totalCount -> {
                    long offset = (long) (pageNumber - 1) * pageSize;

                    Flux<Item> items = search.isBlank() ?
                            itemRepository.findAll(pageSize, offset) :
                            itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                    search, pageSize, offset);

                    return items.flatMap(this::toItemDto)
                            .collectSortedList(order)
                            .map(itemDtos -> {
                                PageableItemsDto dto = new PageableItemsDto();
                                dto.setItems(partitionRows(itemDtos, 5));

                                PageableDto pageable = new PageableDto();
                                pageable.setPageSize(pageSize);
                                pageable.setPageNumber(pageNumber);
                                pageable.setHasPrevious(pageNumber > 1);
                                pageable.setHasNext((offset + pageSize) < totalCount);

                                dto.setPageable(pageable);
                                return dto;
                            });
                });
    }

    private List<List<ItemDto>> partitionRows(List<ItemDto> items, int rowSize) {
        List<List<ItemDto>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i += rowSize) {
            result.add(items.subList(i, Math.min(i + rowSize, items.size())));
        }
        return result;
    }

    private Mono<ItemDto> toItemDto(Item item) {
        return cartRepository.findByItem_Id(item.getId())
                .flatMap(cart -> itemMapper.toItemDto(cart, item))
                .switchIfEmpty(itemMapper.toItemDto(item));
    }

    private static Comparator<ItemDto> sort(SortOption sortOption) {
        switch (sortOption) {
            case ALPHA -> {
                return Comparator.comparing(ItemDto::getTitle, String.CASE_INSENSITIVE_ORDER);
            }
            case PRICE -> {
                return Comparator.comparingDouble(ItemDto::getPrice).reversed();
            }
            case NO -> {
                return Comparator.comparingLong(ItemDto::getId);
            }
        }
        throw new RuntimeException("Invalid sort option");
    }
}
