package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    public Mono<ItemDto> toItemDto(Item item, Long count) {
        return Mono.fromCallable(() -> {
            ItemDto itemDto = new ItemDto();
            itemDto.setId(item.getId());
            itemDto.setTitle(item.getTitle());
            itemDto.setDescription(item.getDescription());
            itemDto.setImgPath(item.getImagePath());
            itemDto.setPrice(item.getPrice());
            itemDto.setCount(count);
            return itemDto;
        });
    }

    public Mono<ItemDto> toItemDto(Item item) {
        return toItemDto(item, 0L); // Default count
    }

    public Mono<ItemDto> toItemDto(Cart cart, Item item) {
        return toItemDto(item, cart.getCount());
    }
}
