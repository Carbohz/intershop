package ru.carbohz.intershop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.exception.OrderNotFoundException;
import ru.carbohz.intershop.mapper.OrderMapper;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.OrderRepository;
import ru.carbohz.intershop.service.OrderService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDto> getOrders() {
        log.trace("returning orders");
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderDto)
                .toList();
    }

    @Override
    public OrderDto getOrderById(long orderId) {
        Optional<Order> maybeOrder = orderRepository.findById(orderId);
        if (maybeOrder.isEmpty()) {
            String message = "Order with id %d not found".formatted(orderId);
            log.error(message);
            throw new OrderNotFoundException(message);
        }

        log.info("Found order with id {}", orderId);
        return orderMapper.toOrderDto(maybeOrder.get());
    }
}
