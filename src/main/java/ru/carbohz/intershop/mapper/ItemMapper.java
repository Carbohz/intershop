package ru.carbohz.intershop.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "imgPath", source =  "item.imagePath")
    ItemDto itemToItemDto(Item item);

    @InheritInverseConfiguration
    Item itemDtoToItem(ItemDto itemDto);
}
