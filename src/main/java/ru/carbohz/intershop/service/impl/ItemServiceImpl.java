package ru.carbohz.intershop.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.service.ItemService;
import ru.carbohz.intershop.service.PageableService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final PageableService pageableService;

    @Override
    public ItemDto findItemById(Long id) {
        Optional<Item> maybeItem = itemRepository.findById(id);
        if (maybeItem.isEmpty()) {
            throw new RuntimeException("Item not found");
        }
        Item item = maybeItem.get();

        return itemMapper.itemToItemDto(item);
    }

    @Override
    public PageableItemsDto getPageableItems(String search, SortOption sort, int pageSize, int pageNumber) {
        Sort sortOption = getSortOption(sort);
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize, sortOption);
        List<Item> found;
        if (!search.isBlank()) {
            found = itemRepository.findByTitleContainingOrDescriptionContainingAllIgnoreCase(search, search, pageRequest);
        } else {
            found = itemRepository.findAll(pageRequest).stream().toList();
        }
        PageableItemsDto pageableItemsDto = new PageableItemsDto();

        int rowSize = 5;
        List<List<ItemDto>> itemDtos = Lists.partition(
                found.stream().map(itemMapper::itemToItemDto).toList(), rowSize);
        pageableItemsDto.setItems(itemDtos);

        long total = itemRepository.count();
        PageableDto pageableDto = new PageableDto();
        pageableDto.setPageSize(pageSize);
        pageableDto.setPageNumber(pageNumber);
        pageableDto.setHasPrevious(pageableService.hasPrevious(pageRequest));
        pageableDto.setHasNext(pageableService.hasNext(pageRequest, total));

        pageableItemsDto.setPageable(pageableDto);

        return pageableItemsDto;
    }

    private static Sort getSortOption(SortOption sort) {
        Sort sortOption;
        switch (sort) {
            case NO -> sortOption =  Sort.unsorted();
            case ALPHA -> sortOption = Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> sortOption = Sort.by("price");
            default -> throw new RuntimeException("Invalid sort option");
        }
        return sortOption;
    }
}
