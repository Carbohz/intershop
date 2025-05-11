package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    public ItemDto itemToItemDto(Item item, Cart cart) {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(item.getId());
        itemDto.setTitle(item.getTitle());
        itemDto.setDescription(item.getDescription());
        itemDto.setImgPath(item.getImagePath());
        itemDto.setCount(cart.getCount());
        itemDto.setPrice(item.getPrice());

        return itemDto;
    }

    public ItemDto itemToItemDto(Item item) {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(item.getId());
        itemDto.setTitle(item.getTitle());
        itemDto.setDescription(item.getDescription());
        itemDto.setImgPath(item.getImagePath());
        itemDto.setPrice(item.getPrice());

        return itemDto;
    }
}
