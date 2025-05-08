package ru.carbohz.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.mapper.OrderMapper;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDto> getOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toOrderDto).toList();
    }

    @Override
    public OrderDto getOrderById(long orderId) {
        Optional<Order> maybeOrder = orderRepository.findById(orderId);
        if (maybeOrder.isEmpty()) {
            throw new RuntimeException();
        }

        return orderMapper.toOrderDto(maybeOrder.get());
    }
}
