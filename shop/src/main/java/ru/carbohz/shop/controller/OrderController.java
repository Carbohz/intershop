package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.dto.OrderDto;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.service.OrderService;

import java.util.Comparator;

import static org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> getOrdersPage(Model model, ServerWebExchange exchange) {
        Comparator<OrderDto> sort = Comparator.comparingLong(OrderDto::id);
        return exchange.getSession().flatMap(webSession -> {
            // Получаем данные пользователя из сессии
            final SecurityContext ctx = webSession.getAttribute(DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
            final Authentication auth = ctx.getAuthentication();
            final User user = (User) auth.getPrincipal();

            return orderService.getOrders(user.getId())
                    .collectSortedList(sort)
                    .doOnNext(orders -> model.addAttribute("orders", orders))
                    .thenReturn("orders");
        });
    }

    @GetMapping("/{id}")
    public Mono<String> getOrderPage(Model model,
                                     @PathVariable("id") long orderId,
                                     @RequestParam(defaultValue = "false") boolean newOrder,
                                     ServerWebExchange exchange) {
        return exchange.getSession().flatMap(webSession -> {
            // Получаем данные пользователя из сессии
            final SecurityContext ctx = webSession.getAttribute(DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
            final Authentication auth = ctx.getAuthentication();
            final User user = (User) auth.getPrincipal();

            return orderService.getOrderById(orderId, user.getId())
                    .doOnNext(order -> {
                        model.addAttribute("order", order);
                        model.addAttribute("newOrder", newOrder);
                    })
                    .thenReturn("order");
        });
    }
}
