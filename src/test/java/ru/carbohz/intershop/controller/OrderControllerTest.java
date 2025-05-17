package ru.carbohz.intershop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.service.OrderService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({OrderController.class})
public class OrderControllerTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getOrdersPage() throws Exception {
        List<OrderDto> orders = new ArrayList<>();

        when(orderService.getOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orders", orders))
                .andExpect(view().name("orders"))
                .andDo(print());

        verify(orderService, times(1)).getOrders();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void getOrderPage() throws Exception {
        long orderId = 1L;
        boolean newOrder = true;

        OrderDto orderDto = new OrderDto();
        when(orderService.getOrderById(orderId)).thenReturn(orderDto);

        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .param("newOrder", String.valueOf(newOrder)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("order", orderDto))
                .andExpect(model().attribute("newOrder", newOrder))
                .andExpect(view().name("order"))
                .andDo(print());

        verify(orderService, times(1)).getOrderById(orderId);
        verifyNoMoreInteractions(orderService);
    }
}
