package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.model.OrderItem;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final ItemMapper itemMapper;

    public CartItemsDto toCartItemsDto(List<Cart> carts) {
        CartItemsDto dto = new CartItemsDto();

        dto.setEmpty(false);
        dto.setItems(carts.stream()
                .map(itemMapper::cartToItemDto)
                .toList());
        dto.setTotal(totalSum(carts));

        return dto;
    }

    public Order toOrder(List<Cart> carts) {
        Order order = new Order();

        List<OrderItem> orderItems = carts.stream()
                .map(cart -> toOrderItem(cart, order))
                .toList();
        order.setOrderItems(orderItems);
        order.setTotalSum(totalSum(carts));

        return order;
    }

    private OrderItem toOrderItem(Cart cart, Order order) {
        OrderItem orderItem = new OrderItem();

        orderItem.setItemId(cart.getItem().getId());
        orderItem.setTitle(cart.getItem().getTitle());
        orderItem.setDescription(cart.getItem().getDescription());
        orderItem.setImagePath(cart.getItem().getImagePath());
        orderItem.setPrice(cart.getItem().getPrice());
        orderItem.setCount(cart.getCount());
        orderItem.setOrder(order);

        return orderItem;
    }

    private Long totalSum(List<Cart> carts) {
        return carts.stream()
                .map(cart -> cart.getCount() * cart.getItem().getPrice())
                .reduce(0L, Long::sum);
    }
}
