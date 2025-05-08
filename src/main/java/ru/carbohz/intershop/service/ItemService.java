package ru.carbohz.intershop.service;

import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.model.SortOption;

public interface ItemService {
    ItemDto findItemById(Long id);

    PageableItemsDto getPageableItems(String search, SortOption sort, int pageSize, int pageNumber);
}
