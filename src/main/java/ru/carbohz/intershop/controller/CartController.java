package ru.carbohz.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Mono<String> getCartItems(Model model) {
        return cartService.getCartItems()
                .doOnNext(cartItemsDto -> {
                    model.addAttribute("items", cartItemsDto.getItems());
                    model.addAttribute("total", cartItemsDto.getTotal());
                    model.addAttribute("empty", cartItemsDto.isEmpty());
                })
                .thenReturn("cart")
                .onErrorResume(e -> {
                    model.addAttribute("error", "Failed to load cart items");
                    return Mono.just("error");
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> changeItemsCount(@PathVariable("id") Long itemId,
                                         @RequestParam Action action) {
        return cartService.changeItemsInCart(itemId, action)
                .thenReturn("redirect:/cart/items")
                .onErrorResume(e -> Mono.just("redirect:/cart/items?error=update_failed"));
    }

    @PostMapping("/buy")
    public Mono<String> buy(Model model) {
        return cartService.createOrder()
                .flatMap(orderId ->
                        Mono.just("redirect:/orders/" + orderId + "?newOrder=true"))
                .onErrorResume(e -> {
                    model.addAttribute("error", "Failed to create order");
                    return Mono.just("cart");
                });
    }
}
