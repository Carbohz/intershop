package ru.carbohz.intershop.service;

import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.model.Action;

public interface CartService {
    Mono<CartItemsDto> getCartItems();
    Mono<Void> changeItemsInCart(Long itemId, Action action);
    Mono<Long> createOrder();
}
