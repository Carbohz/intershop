package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.model.Order;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final ItemMapper itemMapper;

    public OrderDto toOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setItems(order.getItems().stream()
                .map(itemMapper::itemToItemDto)
                .toList());
        orderDto.setTotalSum(order.getItems().stream()
                .map(item -> item.getPrice() * item.getCount())
                .reduce(0L, Long::sum));
        return orderDto;
    }
}
