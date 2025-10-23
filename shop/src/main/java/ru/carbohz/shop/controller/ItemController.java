package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.exception.ItemNotFoundException;
import ru.carbohz.shop.model.Action;
import ru.carbohz.shop.model.SortOption;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.service.CartService;
import ru.carbohz.shop.service.ItemService;

import static org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/main/items")
    public Mono<String> showItems(
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "sort", defaultValue = "NO") SortOption sort,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
            Model model) {

        return itemService.getPageableItems(search, sort, pageSize, pageNumber)
                .doOnNext(pageableItems -> {
                    model.addAttribute("items", pageableItems.getItems());
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("paging", pageableItems.getPageable());
                })
                .thenReturn("main")
                .onErrorResume(e -> {
                    log.error("Error loading items: {}", e.getMessage());
                    model.addAttribute("error", "Could not load products. Please try again.");
                    return Mono.just("main");
                });
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> addItemToCartFromMainPage(
            @PathVariable Long id,
            @RequestParam Action action,
            ServerWebExchange exchange) {

        return exchange.getSession()
                .flatMap(webSession -> {
                    // Получаем данные пользователя из сессии
                    final SecurityContext ctx = webSession.getAttribute(DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
                    final Authentication auth = ctx.getAuthentication();
                    final User user = (User) auth.getPrincipal();

                    return cartService.changeItemsInCart(id, user.getId(), action)
                            .thenReturn("redirect:/main/items")
                            .onErrorResume(e -> {
                                log.warn("Failed to update cart from main page: {}", e.getMessage());
                                return Mono.just("redirect:/main/items");
                            });
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItemById(@PathVariable Long id, Model model) {
        return itemService.findItemById(id)
                .doOnNext(itemDto -> model.addAttribute("item", itemDto))
                .thenReturn("item")
                .onErrorResume(ItemNotFoundException.class, e -> {
                    log.warn("Item not found: {}", id);
                    model.addAttribute("error", "Product not found");
                    return Mono.just("error");
                })
                .onErrorResume(e -> {
                    log.error("Error loading item {}: {}", id, e.getMessage());
                    model.addAttribute("error", "Error loading product details");
                    return Mono.just("error");
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> addItemToCartFromItemPage(
            @PathVariable Long id,
            @RequestParam Action action,
            Model model,
            ServerWebExchange exchange) {

        return exchange.getSession().flatMap(webSession -> {
            // Получаем данные пользователя из сессии
            final SecurityContext ctx = webSession.getAttribute(DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
            final Authentication auth = ctx.getAuthentication();
            final User user = (User) auth.getPrincipal();

            return cartService.changeItemsInCart(id, user.getId(), action)
                    .thenReturn("redirect:/items/" + id)
                    .onErrorResume(e -> {
                        log.warn("Failed to update cart from item page: {}", e.getMessage());
                        model.addAttribute("error", "Failed to update cart");
                        return getItemById(id, model); // Return to item page with error
                    });
        });
    }
}
