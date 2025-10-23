package ru.carbohz.shop.service;

import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.CartItemsDto;
import ru.carbohz.shop.model.Action;

public interface CartService {
    Mono<CartItemsDto> getCartItems(Long userId);
    Mono<Void> changeItemsInCart(Long itemId, Long userId, Action action);
    Mono<Long> createOrder();
}
