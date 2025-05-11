package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.model.Order;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    public OrderDto toOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setItems(order.getOrderItems().stream()
                .map(orderItem -> {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setId(orderItem.getId());
                    itemDto.setTitle(orderItem.getTitle());
                    itemDto.setDescription(orderItem.getDescription());
                    itemDto.setImgPath(orderItem.getImagePath());
                    itemDto.setCount(orderItem.getCount());
                    itemDto.setPrice(orderItem.getPrice());
                    return itemDto;
                })
                .toList());
        orderDto.setTotalSum(order.getOrderItems().stream()
                .map(orderItem -> orderItem.getPrice() * orderItem.getCount())
                .reduce(0L, Long::sum));
        return orderDto;
    }
}
