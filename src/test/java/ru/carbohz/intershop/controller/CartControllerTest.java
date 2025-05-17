package ru.carbohz.intershop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.carbohz.intershop.dto.CartItemsDto;
import ru.carbohz.intershop.model.Action;
import ru.carbohz.intershop.service.CartService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CartController.class})
public class CartControllerTest {

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getCartItems() throws Exception {
        CartItemsDto cartItemsDto = new CartItemsDto();

        when(cartService.getCartItems()).thenReturn(cartItemsDto);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", cartItemsDto.getItems()))
                .andExpect(model().attribute("total", cartItemsDto.getTotal()))
                .andExpect(model().attribute("empty", cartItemsDto.isEmpty()))
                .andDo(print());

        verify(cartService, times(1)).getCartItems();
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void changeItemsCount() throws Exception {
        Long itemId = 1L;
        Action action = Action.DELETE;
        doNothing().when(cartService).changeItemsInCart(itemId, action);

        mockMvc.perform(post("/cart/items/{itemId}", itemId)
                        .param("action", action.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/cart/items"))
                .andExpect(redirectedUrl("/cart/items"))
                .andDo(print());

        verify(cartService, times(1)).changeItemsInCart(itemId, action);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void buy() throws Exception {
        Long orderId = 1L;

        when(cartService.createOrder()).thenReturn(orderId);

        mockMvc.perform(post("/cart/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/orders/%d?newOrder=true".formatted(orderId)))
                .andExpect(redirectedUrl("/orders/%d?newOrder=true".formatted(orderId)))
                .andDo(print());

        verify(cartService, times(1)).createOrder();
        verifyNoMoreInteractions(cartService);
    }
}
