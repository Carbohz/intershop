package ru.carbohz.shop.service;

import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.dto.PageableItemsDto;
import ru.carbohz.shop.model.SortOption;

public interface ItemService {
    Mono<ItemDto> findItemById(Long id);
    Mono<PageableItemsDto> getPageableItems(String search, SortOption sort, int pageSize, int pageNumber);
}
