package ru.carbohz.shop.mapper;

import org.springframework.stereotype.Component;
import ru.carbohz.shop.dto.ItemDto;
import ru.carbohz.shop.model.OrderItem;

@Component
public class OrderItemMapper {
    public ItemDto toItemDto(OrderItem orderItem) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(orderItem.getItemId());
        itemDto.setTitle(orderItem.getTitle());
        itemDto.setDescription(orderItem.getDescription());
        itemDto.setImgPath(orderItem.getImagePath());
        itemDto.setCount(orderItem.getCount());
        itemDto.setPrice(orderItem.getPrice());
        return itemDto;
    }
}
