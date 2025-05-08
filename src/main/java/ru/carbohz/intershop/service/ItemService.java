package ru.carbohz.intershop.service;

import ru.carbohz.intershop.dto.ItemDto;

public interface ItemService {
    ItemDto findItemById(Long id);
}
