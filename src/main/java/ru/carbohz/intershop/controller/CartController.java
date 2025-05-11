package ru.carbohz.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public String getCartItems(Model model) {
        CartItemsDto cartItemsDto = cartService.getCartItems();

        model.addAttribute("items", cartItemsDto.getItems());
        model.addAttribute("total", cartItemsDto.getTotal());
        model.addAttribute("empty", cartItemsDto.isEmpty());

        return "cart";
    }

    @PostMapping("/items/{id}")
    public String changeItemsCount(@PathVariable("id") Long itemId, @RequestParam Action action) {
        cartService.changeItemsInCart(itemId, action);
        return "redirect:/cart/items";
    }

    @PostMapping("/buy")
    public String buy() {
        Long orderId = cartService.createOrder();
        return "redirect:/orders/%d?newOrder=true".formatted(orderId);
    }
}
