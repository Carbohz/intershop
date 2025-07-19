package ru.carbohz.intershop.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.model.OrderItem;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final ItemMapper itemMapper;

    public Mono<CartItemsDto> toCartItemsDto(List<Cart> carts, Map<Long, Item> itemMap) {
        return Flux.fromIterable(carts)
                .flatMap(cart -> {
                    Item item = itemMap.get(cart.getItemId());
                    if (item != null) {
                        return itemMapper.toItemDto(cart, item);
                    }
                    return Mono.empty();
                })
                .collectList()
                .map(items -> {
                    CartItemsDto dto = new CartItemsDto();
                    dto.setItems(items);
                    dto.setEmpty(items.isEmpty());
                    dto.setTotal(totalSum(carts, itemMap));
                    return dto;
                });
    }

    public Order toOrder(List<Cart> carts, Map<Long, Item> itemMap) {
        Order order = new Order();
        order.setTotalSum(totalSum(carts, itemMap));
        return order;
    }

    private Long totalSum(List<Cart> carts, Map<Long, Item> itemMap) {
        return carts.stream()
                .map(cart -> {
                    Item item = itemMap.get(cart.getItemId());
                    return item != null ? cart.getCount() * item.getPrice() : 0L;
                })
                .reduce(0L, Long::sum);
    }

    public List<OrderItem> toOrderItems(List<Cart> carts, Long orderId, Map<Long, Item> itemMap) {
        return carts.stream()
                .map(cart -> {
                    Item item = itemMap.get(cart.getItemId());
                    if (item == null) return null;

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(orderId);
                    orderItem.setItemId(cart.getItemId());
                    orderItem.setTitle(item.getTitle());
                    orderItem.setDescription(item.getDescription());
                    orderItem.setImagePath(item.getImagePath());
                    orderItem.setPrice(item.getPrice());
                    orderItem.setCount(cart.getCount());
                    return orderItem;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
