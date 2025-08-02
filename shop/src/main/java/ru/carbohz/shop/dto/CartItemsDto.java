package ru.carbohz.shop.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class CartItemsDto {
    private List<ItemDto> items;
    private Long total;
    private boolean empty;
}
