package ru.carbohz.intershop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.model.OrderItem;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;
import ru.carbohz.intershop.service.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public CartItemsDto getCartItems() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            return new CartItemsDto(new ArrayList<>(), 0L, true);
        }
        CartItemsDto dto = new CartItemsDto();
        dto.setEmpty(false);
        dto.setItems(carts.stream()
                .map(cart -> {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setId(cart.getItem().getId());
                    itemDto.setTitle(cart.getItem().getTitle());
                    itemDto.setDescription(cart.getItem().getDescription());
                    itemDto.setImgPath(cart.getItem().getImagePath());
                    itemDto.setCount(cart.getCount());
                    itemDto.setPrice(cart.getItem().getPrice());
                    return itemDto;
                })
                .toList());
        dto.setTotal(carts.stream()
                .map(cart -> cart.getCount() * cart.getItem().getPrice())
                .reduce(0L, Long::sum));

        return dto;
    }

    @Override
    @Transactional
    public void changeItemsInCart(Long itemId, Action action) {
        switch (action) {
            case PLUS -> {
                Optional<Cart> maybeCart = cartRepository.findByItem_Id(itemId);
                if (maybeCart.isEmpty()) {
                    Cart cart = new Cart();
                    cart.setCount(1L);
                    itemRepository.findById(itemId).ifPresent(cart::setItem);
                    cartRepository.save(cart);
                    return;
                }
                cartRepository.increaseCountForItem(itemId);
            }
            case MINUS -> {
                Optional<Cart> maybeCart = cartRepository.findByItem_Id(itemId);
                if (maybeCart.isEmpty()) {
                    return;
                }

                Cart cart = maybeCart.get();
                if (cart.getCount() == 1L) {
                    cartRepository.deleteByItem_Id(itemId);
                    return;
                }

                cartRepository.decreaseCountForItem(itemId);
            }
            case DELETE -> cartRepository.deleteByItem_Id(itemId);
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
    }

    @Override
    @Transactional
    public Long createOrder() {
        Order order = new Order();

        List<Cart> carts = cartRepository.findAll();

        List<OrderItem> orderItems = carts.stream()
                .map(cart -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setTitle(cart.getItem().getTitle());
                    orderItem.setDescription(cart.getItem().getDescription());
                    orderItem.setImagePath(cart.getItem().getImagePath());
                    orderItem.setPrice(cart.getItem().getPrice());
                    orderItem.setCount(cart.getCount());
                    orderItem.setOrder(order);
                    return orderItem;
                }).toList();
        order.setOrderItems(orderItems);
        order.setTotalSum(carts.stream()
                .map(cart -> cart.getCount() * cart.getItem().getPrice())
                .reduce(0L, Long::sum));

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteAll();

        return savedOrder.getId();
    }
}
