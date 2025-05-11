package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.model.Order;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;

    public OrderDto toOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setItems(order.getOrderItems().stream()
                .map(orderItemMapper::toItemDto)
                .toList());
        orderDto.setTotalSum(order.getTotalSum());
        return orderDto;
    }
}
