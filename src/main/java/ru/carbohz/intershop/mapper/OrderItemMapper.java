package ru.carbohz.intershop.mapper;

import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.model.OrderItem;

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
