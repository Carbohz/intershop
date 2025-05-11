package ru.carbohz.intershop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.carbohz.intershop.dto.ItemDto;
import ru.carbohz.intershop.dto.OrderDto;
import ru.carbohz.intershop.exception.OrderNotFoundException;
import ru.carbohz.intershop.mapper.OrderMapper;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.model.OrderItem;
import ru.carbohz.intershop.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        OrderServiceImpl.class,
        OrderMapper.class
})
class OrderServiceImplTest {
    @Autowired
    OrderServiceImpl orderService;

    @MockitoBean
    OrderRepository orderRepository;

    @Test
    void getOrders() {
        List<Order> orders = new ArrayList<>();
        // create order 1
        Order order1 = createOrder();

        // create order 2
        Order order2 = new Order();
        order2.setId(2L);
        order2.setTotalSum(1337L);

        List<OrderItem> orderItems2 = new ArrayList<>();

        OrderItem orderItem3 = new OrderItem();
        orderItem3.setId(3L);
        orderItem3.setTitle("Item 3");
        orderItem3.setDescription("Item 3 description");
        orderItem3.setImagePath("Item 3 image path");
        orderItem3.setPrice(837L);
        orderItem3.setCount(1L);
        orderItem3.setOrder(order2);

        orderItems2.add(orderItem3);

        order2.setOrderItems(orderItems2);

        orders.add(order1);
        orders.add(order2);

        when(orderRepository.findAll()).thenReturn(orders);

        List<OrderDto> orderDtos = orderService.getOrders();

        assertThat(orderDtos.size()).isEqualTo(2);

        OrderDto orderDto1 = orderDtos.get(0);
        assertThat(orderDto1.id()).isEqualTo(1L);
        assertThat(orderDto1.totalSum()).isEqualTo(100500L);

        List<ItemDto> itemDtos1 = orderDto1.items();

        ItemDto itemDto1 = itemDtos1.get(0);
        assertThat(itemDto1.getId()).isEqualTo(1L);
        assertThat(itemDto1.getTitle()).isEqualTo("Item 1");
        assertThat(itemDto1.getDescription()).isEqualTo("Item 1 description");
        assertThat(itemDto1.getImgPath()).isEqualTo("Item 1 image path");
        assertThat(itemDto1.getCount()).isEqualTo(1L);
        assertThat(itemDto1.getPrice()).isEqualTo(100000L);

        ItemDto itemDto2 = itemDtos1.get(1);
        assertThat(itemDto2.getId()).isEqualTo(2L);
        assertThat(itemDto2.getTitle()).isEqualTo("Item 2");
        assertThat(itemDto2.getDescription()).isEqualTo("Item 2 description");
        assertThat(itemDto2.getImgPath()).isEqualTo("Item 2 image path");
        assertThat(itemDto2.getCount()).isEqualTo(2L);
        assertThat(itemDto2.getPrice()).isEqualTo(250L);

        OrderDto orderDto2 = orderDtos.get(1);
        assertThat(orderDto2.id()).isEqualTo(2L);
        assertThat(orderDto2.totalSum()).isEqualTo(837L);

        List<ItemDto> itemDtos2 = orderDto2.items();

        ItemDto itemDto3 = itemDtos2.get(0);
        assertThat(itemDto3.getId()).isEqualTo(3L);
        assertThat(itemDto3.getTitle()).isEqualTo("Item 3");
        assertThat(itemDto3.getDescription()).isEqualTo("Item 3 description");
        assertThat(itemDto3.getImgPath()).isEqualTo("Item 3 image path");
        assertThat(itemDto3.getCount()).isEqualTo(1L);
        assertThat(itemDto3.getPrice()).isEqualTo(837L);

        verify(orderRepository, times(1)).findAll();
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void getOrderById() {
        Order order = createOrder();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDto orderDto1 = orderService.getOrderById(1L);

        assertThat(orderDto1.id()).isEqualTo(1L);
        assertThat(orderDto1.totalSum()).isEqualTo(100500L);

        List<ItemDto> itemDtos1 = orderDto1.items();

        ItemDto itemDto1 = itemDtos1.get(0);
        assertThat(itemDto1.getId()).isEqualTo(1L);
        assertThat(itemDto1.getTitle()).isEqualTo("Item 1");
        assertThat(itemDto1.getDescription()).isEqualTo("Item 1 description");
        assertThat(itemDto1.getImgPath()).isEqualTo("Item 1 image path");
        assertThat(itemDto1.getCount()).isEqualTo(1L);
        assertThat(itemDto1.getPrice()).isEqualTo(100000L);

        ItemDto itemDto2 = itemDtos1.get(1);
        assertThat(itemDto2.getId()).isEqualTo(2L);
        assertThat(itemDto2.getTitle()).isEqualTo("Item 2");
        assertThat(itemDto2.getDescription()).isEqualTo("Item 2 description");
        assertThat(itemDto2.getImgPath()).isEqualTo("Item 2 image path");
        assertThat(itemDto2.getCount()).isEqualTo(2L);
        assertThat(itemDto2.getPrice()).isEqualTo(250L);

        verify(orderRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void getOrderById_whenUserNotFound_throwsUserNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order with id 1 not found");

    }

    private Order createOrder() {
        // create order 1
        Order order1 = new Order();
        order1.setId(1L);
        order1.setTotalSum(100500L);

        List<OrderItem> orderItems1 = new ArrayList<>();

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setTitle("Item 1");
        orderItem1.setDescription("Item 1 description");
        orderItem1.setImagePath("Item 1 image path");
        orderItem1.setPrice(100000L);
        orderItem1.setCount(1L);
        orderItem1.setOrder(order1);

        orderItems1.add(orderItem1);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        orderItem2.setTitle("Item 2");
        orderItem2.setDescription("Item 2 description");
        orderItem2.setImagePath("Item 2 image path");
        orderItem2.setPrice(250L);
        orderItem2.setCount(2L);
        orderItem2.setOrder(order1);

        orderItems1.add(orderItem2);

        order1.setOrderItems(orderItems1);

        return order1;
    }
}