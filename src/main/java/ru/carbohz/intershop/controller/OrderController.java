package ru.carbohz.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.service.OrderService;

import java.util.Comparator;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> getOrdersPage(Model model) {
        Comparator<OrderDto> sort = Comparator.comparingLong(OrderDto::id);
        return orderService.getOrders()
                .collectSortedList(sort)
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/{id}")
    public Mono<String> getOrderPage(Model model,
                                     @PathVariable("id") long orderId,
                                     @RequestParam(defaultValue = "false") boolean newOrder) {
        return orderService.getOrderById(orderId)
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }
}
