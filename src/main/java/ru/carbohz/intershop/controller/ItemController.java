package ru.carbohz.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/main/items")
    public String getItems(@RequestParam(name = "search", defaultValue = "") String search,
                           @RequestParam(name = "sort", defaultValue = "NO") SortOption sort,
                           @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                           @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                           Model model) {
        PageableItemsDto pageableItems = itemService.getPageableItems(search, sort, pageSize, pageNumber);

        model.addAttribute("items", pageableItems.getItems());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", pageableItems.getPageable());

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
