package ru.carbohz.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageableItemsDto {
    List<List<ItemDto>> items;
    PageableDto pageable;
}
