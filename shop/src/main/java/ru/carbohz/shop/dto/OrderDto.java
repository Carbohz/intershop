package ru.carbohz.shop.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@ToString
public class OrderDto {
    private long id;
    private List<ItemDto> items;
    private Long totalSum;

    public long id() {
        return this.id;
    }

    public List<ItemDto> items() {
        return this.items;
    }

    public Long totalSum() {
        return this.totalSum;
    }
}
