package ru.carbohz.intershop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private Long count;
    private Long price;
}
