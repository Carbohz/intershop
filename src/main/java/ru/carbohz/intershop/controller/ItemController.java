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
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.service.CartService;
import ru.carbohz.intershop.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;

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

        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String addItemToCart(@PathVariable Long id, @RequestParam Action action) {
        cartService.changeItemsInCart(id, action);
        return "redirect:/main/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, final Model model) {
        ItemDto itemDto = itemService.findItemById(id);

        model.addAttribute("item", itemDto);

        return "item";
    }

    @PostMapping("/items/{id}")
    public String addItemToCartV2(@PathVariable Long id, @RequestParam Action action) {
        cartService.changeItemsInCart(id, action);
        return "redirect:/items/" + id;
    }
}
