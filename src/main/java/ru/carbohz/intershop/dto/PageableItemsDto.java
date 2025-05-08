package ru.carbohz.intershop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageableItemsDto {
    List<List<ItemDto>> items;
    PageableDto pageable;
}
