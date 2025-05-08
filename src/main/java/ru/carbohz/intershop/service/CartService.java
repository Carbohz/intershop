package ru.carbohz.intershop.service;

import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.model.Action;

public interface CartService {
    CartItemsDto getCartItems();
    void changeItemsInCart(Long itemId, Action action);
    Long createOrder();
}
