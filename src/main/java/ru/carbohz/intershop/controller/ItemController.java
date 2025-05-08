package ru.carbohz.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/main/items")
    public String getItems(Model model) {
        return "main.html";
    }

    @PostMapping("/main/items/{id}")
    public String addItemToCart(@PathVariable Integer id) {
        return "redirect:/main/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, final Model model) {
        ItemDto itemDto = itemService.findItemById(id);

        model.addAttribute("item", itemDto);

        return "item.html";
    }

    @PostMapping("/items/{id}")
    public String addItemToCart(@PathVariable Integer id, Model model) {
        return "item.html";
    }
}
