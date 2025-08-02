package ru.carbohz.shop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.OrderDto;
import ru.carbohz.shop.model.Order;
import ru.carbohz.shop.model.OrderItem;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;

    public Mono<OrderDto> toOrderDto(Order order, List<OrderItem> orderItems) {
        return Mono.fromCallable(() -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setId(order.getId());
            orderDto.setItems(orderItems.stream()
                    .map(orderItemMapper::toItemDto)
                    .toList());
            orderDto.setTotalSum(order.getTotalSum());
            return orderDto;
        });
    }
}