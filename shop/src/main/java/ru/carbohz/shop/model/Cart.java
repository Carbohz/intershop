package ru.carbohz.shop.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "carts")
@Getter
@Setter
@ToString
public class Cart {
    @Id
    private Long id;

    @Column
    private Long count;

    @Column
    private Long itemId;
}
