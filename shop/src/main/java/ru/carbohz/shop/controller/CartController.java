package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.Action;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Mono<String> getCartItems(Model model) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .cast(User.class)
                .flatMap(user -> cartService.getCartItems(user.getId())
                        .doOnNext(cartItemsDto -> {
                            model.addAttribute("items", cartItemsDto.getItems());
                            model.addAttribute("total", cartItemsDto.getTotal());
                            model.addAttribute("empty", cartItemsDto.isEmpty());
                        })
                        .thenReturn("cart")
                        .onErrorResume(e -> {
                            model.addAttribute("error", "Failed to load cart items");
                            return Mono.just("error");
                        }));
    }

    @PostMapping("/items/{id}")
    public Mono<String> changeItemsCount(@PathVariable("id") Long itemId,
                                         @RequestParam Action action) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .cast(User.class)
                .flatMap(user -> cartService.changeItemsInCart(itemId, user.getId(), action)
                        .thenReturn("redirect:/cart/items")
                        .onErrorResume(e -> Mono.just("redirect:/cart/items?error=update_failed")));
    }

    @PostMapping("/buy")
    public Mono<String> buy(Model model) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .cast(User.class)
                .flatMap(user -> cartService.createOrder(user.getId())
                        .flatMap(orderId ->
                                Mono.just("redirect:/orders/" + orderId + "?newOrder=true"))
                        .onErrorResume(e -> {
                            model.addAttribute("error", "Failed to create order: " + e.getMessage());
                            return Mono.just("error");
                        }));
    }
}
