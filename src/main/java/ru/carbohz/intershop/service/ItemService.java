package ru.carbohz.intershop.service;

import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.model.SortOption;

public interface ItemService {
    Mono<ItemDto> findItemById(Long id);
    Mono<PageableItemsDto> getPageableItems(String search, SortOption sort, int pageSize, int pageNumber);
}
