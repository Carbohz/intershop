package ru.carbohz.intershop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.service.ItemService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto findItemById(Long id) {
        Optional<Item> maybeItem = itemRepository.findById(id);
        if (maybeItem.isEmpty()) {
            throw new RuntimeException("Item not found");
        }
        Item item = maybeItem.get();

        return itemMapper.itemToItemDto(item);
    }
}
