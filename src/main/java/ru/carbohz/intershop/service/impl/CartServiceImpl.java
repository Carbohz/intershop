package ru.carbohz.intershop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.mapper.CartMapper;
import ru.carbohz.intershop.model.*;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;
import ru.carbohz.intershop.service.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Mono<CartItemsDto> getCartItems() {
        return cartRepository.findAll()
                .collectList()
                .flatMap(carts -> {
                    if (carts.isEmpty()) {
                        return Mono.just(new CartItemsDto(new ArrayList<>(), 0L, true));
                    }

                    return fetchItemsForCarts(carts)
                            .flatMap(itemMap -> cartMapper.toCartItemsDto(carts, itemMap));
                });
    }

    private Mono<Map<Long, Item>> fetchItemsForCarts(List<Cart> carts) {
        List<Long> itemIds = carts.stream()
                .map(Cart::getItemId)
                .distinct()
                .toList();

        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId, Function.identity());
    }

    @Override
    @Transactional
    public Mono<Void> changeItemsInCart(Long itemId, Action action) {
        return switch (action) {
            case PLUS -> handlePlusAction(itemId);
            case MINUS -> handleMinusAction(itemId);
            case DELETE -> handleDeleteAction(itemId);
        };
    }

    private Mono<Void> handlePlusAction(Long itemId) {
        return cartRepository.findByItem_Id(itemId)
                .flatMap(cart -> {
                    cart.setCount(cart.getCount() + 1);
                    return cartRepository.save(cart);
                })
                .switchIfEmpty(
                        itemRepository.findById(itemId)
                                .flatMap(item -> {
                                    Cart newCart = new Cart();
                                    newCart.setItemId(item.getId());
                                    newCart.setCount(1L);
                                    return cartRepository.save(newCart);
                                })
                )
                .then();
    }

    private Mono<Void> handleMinusAction(Long itemId) {
        return cartRepository.findByItem_Id(itemId)
                .flatMap(cart -> {
                    if (cart.getCount() == 1) {
                        return cartRepository.deleteById(cart.getId());
                    } else {
                        cart.setCount(cart.getCount() - 1);
                        return cartRepository.save(cart);
                    }
                })
                .then();
    }

    private Mono<Void> handleDeleteAction(Long itemId) {
        return cartRepository.deleteByItem_Id(itemId);
    }

    @Override
    @Transactional
    public Mono<Long> createOrder() {
        return cartRepository.findAll()
                .collectList()
                .flatMap(carts -> {
                    if (carts.isEmpty()) {
                        return Mono.error(new IllegalStateException("Cart is empty"));
                    }

                    return fetchItemsForCarts(carts)
                            .flatMap(itemMap -> {
                                Order order = cartMapper.toOrder(carts, itemMap);
                                return orderRepository.save(order)
                                        .flatMap(savedOrder -> {
                                            List<OrderItem> orderItems =
                                                    cartMapper.toOrderItems(carts, savedOrder.getId(), itemMap);

                                            return orderItemRepository.saveAll(orderItems)
                                                    .then(cartRepository.deleteAll())
                                                    .thenReturn(savedOrder.getId());
                                        });
                            });
                });
    }
}
