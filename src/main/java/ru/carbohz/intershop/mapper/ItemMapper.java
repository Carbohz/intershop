package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.repository.CartRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final CartRepository cartRepository;

    public ItemDto itemToItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setTitle(item.getTitle());
        itemDto.setDescription(item.getDescription());
        itemDto.setImgPath(item.getImagePath());
        Optional<Cart> maybeCart = cartRepository.findByItem_Id(item.getId());
        maybeCart.ifPresent(cart -> itemDto.setCount(cart.getCount()));
        itemDto.setPrice(item.getPrice());
        return itemDto;
    }
}
