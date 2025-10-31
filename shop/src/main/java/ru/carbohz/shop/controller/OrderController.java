package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.OrderDto;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.service.OrderService;

import java.util.Comparator;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> getOrdersPage(Model model) {
        Comparator<OrderDto> sort = Comparator.comparingLong(OrderDto::id);
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .cast(User.class)
                .flatMap(user -> orderService.getOrders(user.getId())
                        .collectSortedList(sort)
                        .doOnNext(orders -> model.addAttribute("orders", orders))
                        .thenReturn("orders"));
    }

    @GetMapping("/{id}")
    public Mono<String> getOrderPage(Model model,
                                     @PathVariable("id") long orderId,
                                     @RequestParam(defaultValue = "false") boolean newOrder) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .cast(User.class)
                .flatMap(user -> {
                    return orderService.getOrderById(orderId, user.getId())
                            .doOnNext(order -> {
                                model.addAttribute("order", order);
                                model.addAttribute("newOrder", newOrder);
                            })
                            .thenReturn("order");
                });
    }
}
