package ru.carbohz.shop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {
    @Id
    private Long id;

    @Column
    private Long itemId;

    @Column
    private Long orderId;

    @Column
    private Long userId;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String imagePath;

    @Column
    private Long price;

    @Column
    private Long count;
}
