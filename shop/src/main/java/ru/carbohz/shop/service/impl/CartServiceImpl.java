package ru.carbohz.shop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.api.PaymentApi;
import ru.carbohz.shop.api.model.BalancePostRequest;
import ru.carbohz.shop.dto.CartItemsDto;
import ru.carbohz.shop.mapper.CartMapper;
import ru.carbohz.shop.model.*;
import ru.carbohz.shop.repository.CartRepository;
import ru.carbohz.shop.repository.ItemRepository;
import ru.carbohz.shop.repository.OrderItemRepository;
import ru.carbohz.shop.repository.OrderRepository;
import ru.carbohz.shop.service.CartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final OrderItemRepository orderItemRepository;
    private final PaymentApi paymentApi;

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

                                final var balanceRequest = new BalancePostRequest();
                                balanceRequest.setSum(BigDecimal.valueOf(order.getTotalSum()));
                                return paymentApi.balancePostWithHttpInfo(balanceRequest)
                                        .flatMap(resp -> {
                                            if (resp.getStatusCode().is2xxSuccessful()) {
                                                log.info("enough balance, processing order");
                                                return orderRepository.save(order)
                                                        .flatMap(savedOrder -> {
                                                            List<OrderItem> orderItems =
                                                                    cartMapper.toOrderItems(carts, savedOrder.getId(), itemMap);

                                                            return orderItemRepository.saveAll(orderItems)
                                                                    .then(cartRepository.deleteAll())
                                                                    .thenReturn(savedOrder.getId());
                                                        });
                                            } else {
                                                log.info("failed to save order: not enough balance");
                                                return Mono.error(new IllegalStateException("not enough balance"));
                                            }
                                        });
//                                return orderRepository.save(order)
//                                        .flatMap(savedOrder -> {
//                                            List<OrderItem> orderItems =
//                                                    cartMapper.toOrderItems(carts, savedOrder.getId(), itemMap);
//
//                                            return orderItemRepository.saveAll(orderItems)
//                                                    .then(cartRepository.deleteAll())
//                                                    .thenReturn(savedOrder.getId());
//                                        });

                            });
                });
    }
}
