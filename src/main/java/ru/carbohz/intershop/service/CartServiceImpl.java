package ru.carbohz.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.mapper.ItemMapper;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public CartItemsDto getCartItems() {
        List<Item> items = itemRepository.findAllByCountIsGreaterThan(0L);
        if (items.isEmpty()) {
            return new CartItemsDto(new ArrayList<>(), 0L, true);
        }

        CartItemsDto dto = new CartItemsDto();
        dto.setEmpty(false);
        dto.setItems(items.stream().map(itemMapper::itemToItemDto).toList());
        dto.setTotal(items.stream()
                .map(item -> item.getCount() * item.getPrice())
                .reduce(0L, Long::sum));

        return dto;
    }

    @Override
    @Transactional
    public void changeItemsInCart(Long itemId, Action action) {
        switch (action) {
            case PLUS -> itemRepository.increaseCount(itemId);
            case MINUS -> itemRepository.decreaseCount(itemId);
            case DELETE -> itemRepository.resetCount(itemId);
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
    }

    @Override
    @Transactional
    public Long createOrder() {
        Order order = new Order();
        List<Item> items = itemRepository.findAll()
                .stream()
                .filter(item -> item.getCount() > 0)
                .toList();
        order.setItems(items);
        Order savedOrder = orderRepository.save(order);
        // TODO так не получится, потому что у новых заказов будет всегда 0 предметов
        itemRepository.resetCountForAll();
        return savedOrder.getId();
    }
}
