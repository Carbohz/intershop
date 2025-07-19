package ru.carbohz.intershop.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.exception.ItemNotFoundException;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.service.ItemService;
import ru.carbohz.intershop.service.PageableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final PageableService pageableService;
    private final CartRepository cartRepository;

    @Override
    public Mono<ItemDto> findItemById(Long id) {
//        Optional<Item> maybeItem = itemRepository.findById(id);
//        if (maybeItem.isEmpty()) {
//            String message =  "Item with id %d not found".formatted(id);
//            log.error(message);
//            throw new ItemNotFoundException(message);
//        }
//        Item item = maybeItem.get();
//
//        return toItemDto(item);
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
    public Mono<PageableItemsDto> getPageableItems(String search, SortOption sort, int pageSize, int pageNumber) {
//        Sort sortOption = getSortOption(sort);
//        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize, sortOption);
//        List<Item> found;
//        if (!search.isBlank()) {
//            found = itemRepository.findByTitleContainingOrDescriptionContainingAllIgnoreCase(search, search, pageRequest);
//        } else {
//            found = itemRepository.findAll(pageRequest).stream().toList();
//        }
//        PageableItemsDto pageableItemsDto = new PageableItemsDto();
//
//        int rowSize = 5;
//        List<List<ItemDto>> itemDtos = Lists.partition(
//                found.stream().map(this::toItemDto).toList(), rowSize);
//        pageableItemsDto.setItems(itemDtos);
//
//        long total = itemRepository.count();
//        PageableDto pageableDto = new PageableDto();
//        pageableDto.setPageSize(pageSize);
//        pageableDto.setPageNumber(pageNumber);
//        pageableDto.setHasPrevious(pageableService.hasPrevious(pageRequest));
//        pageableDto.setHasNext(pageableService.hasNext(pageRequest, total));
//
//        pageableItemsDto.setPageable(pageableDto);
//
//        return pageableItemsDto;

        Sort sortOption = getSortOption(sort);
        String sortOptionStr = getSortOptionStr(sort);
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize, sortOption);

        return (search.isBlank() ? itemRepository.count() :
                itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search))
                .flatMap(totalCount -> {
                    long offset = pageRequest.getOffset();

                    Flux<Item> items = search.isBlank() ?
                            itemRepository.findAll(pageSize, offset) :
                            itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                    search, sortOptionStr, pageSize, offset);

                    return items.flatMap(this::toItemDto)
                            .collectList()
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

    private static Sort getSortOption(SortOption sort) {
        Sort sortOption;
        switch (sort) {
            case NO -> sortOption =  Sort.unsorted();
            case ALPHA -> sortOption = Sort.by("title");
            case PRICE -> sortOption = Sort.by("price");
            default -> throw new RuntimeException("Invalid sort option");
        }
        return sortOption;
    }

    private static String getSortOptionStr(SortOption sort) {
        String sortOption;
        switch (sort) {
            case NO -> sortOption =  "";
            case ALPHA -> sortOption = "title";
            case PRICE -> sortOption = "price";
            default -> throw new RuntimeException("Invalid sort option");
        }
        return sortOption;
    }
}
