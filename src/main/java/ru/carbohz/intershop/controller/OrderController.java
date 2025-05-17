package ru.carbohz.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String getOrdersPage(Model model) {
        List<OrderDto> orders = orderService.getOrders();

        model.addAttribute("orders", orders);

        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrderPage(Model model,
                               @PathVariable("id") long orderId,
                               @RequestParam(defaultValue = "false") boolean newOrder) {
        OrderDto order = orderService.getOrderById(orderId);

        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
