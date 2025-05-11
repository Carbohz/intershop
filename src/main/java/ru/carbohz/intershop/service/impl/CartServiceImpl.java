package ru.carbohz.intershop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.mapper.CartMapper;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Order;
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
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartItemsDto getCartItems() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            return new CartItemsDto(new ArrayList<>(), 0L, true);
        }

        return cartMapper.toCartItemsDto(carts);
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
        List<Cart> carts = cartRepository.findAll();

        Order order = cartMapper.toOrder(carts);

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteAll();

        return savedOrder.getId();
    }
}
