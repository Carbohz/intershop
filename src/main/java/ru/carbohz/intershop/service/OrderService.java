package ru.carbohz.intershop.service;

import ru.carbohz.intershop.dto.OrderDto;

import java.util.List;

public interface OrderService {
    List<OrderDto> getOrders();
    OrderDto getOrderById(long orderId);
}
