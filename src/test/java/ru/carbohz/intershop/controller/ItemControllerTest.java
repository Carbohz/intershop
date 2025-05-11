package ru.carbohz.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.carbohz.intershop.dto.PageableDto;
import ru.carbohz.intershop.dto.PageableItemsDto;
import ru.carbohz.intershop.model.SortOption;
import ru.carbohz.intershop.service.CartService;
import ru.carbohz.intershop.service.ItemService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ItemController.class})
public class ItemControllerTest {

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void showItems() throws Exception {
        String search = "random";
        SortOption sort = SortOption.PRICE;
        int pageSize = 5;
        int pageNumber = 2;

        PageableItemsDto pageableItems = new PageableItemsDto();
        PageableDto pageableDto = new PageableDto();
        pageableDto.setPageSize(pageSize);
        pageableDto.setPageNumber(pageNumber);
        pageableItems.setPageable(pageableDto);

        when(itemService.getPageableItems(search, sort, pageSize, pageNumber)).thenReturn(pageableItems);

        mockMvc.perform(get("/main/items")
                        .param("search", search)
                        .param("sort", sort.toString())
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber)))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("items", pageableItems.getItems()))
                .andExpect(model().attribute("sort", sort))
                .andExpect(model().attribute("paging", pageableItems.getPageable()))
                .andExpect(model().attribute("search", search))
                .andDo(print());
    }

    @Test
    public void addItemToCartFromMainPage() throws Exception {
        mockMvc.perform(post("/main/items/{0}", "0")
                        .param("action", ""))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getItemById() throws Exception {
        mockMvc.perform(get("/items/{0}", "0"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void addItemToCartFromItemPage() throws Exception {
        mockMvc.perform(post("/items/{0}", "0")
                        .param("action", ""))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
